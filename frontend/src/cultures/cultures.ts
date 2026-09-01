import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CultureService } from '../app/core/api/culture.service';
import { FermentTypeService } from '../app/core/api/ferment-type.service';
import {
  Culture,
  CultureFeeding,
  CultureHealth,
  CultureKind,
  CultureTreeNode,
  FermentType
} from '../app/core/api/models';
import { ChartComponent } from '../shared/chart/chart';
import { CHART_PALETTE, CHART_TEXT_COLOR, EChartsOption } from '../shared/chart/echarts.setup';
import {
  CULTURE_HEALTHS,
  CULTURE_HEALTH_CLASS,
  CULTURE_KINDS,
  humanise,
  relativeDays
} from '../shared/labels';
import { ToastService } from '../shared/toast.service';

@Component({
  selector: 'app-cultures',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe, ChartComponent],
  templateUrl: './cultures.html'
})
export class CulturesComponent {
  private readonly cultureService = inject(CultureService);
  private readonly fermentTypeService = inject(FermentTypeService);
  private readonly toastService = inject(ToastService);

  protected readonly cultures = signal<Culture[]>([]);
  protected readonly tree = signal<CultureTreeNode[]>([]);
  protected readonly fermentTypes = signal<FermentType[]>([]);
  protected readonly loading = signal(true);
  protected readonly busy = signal(false);

  protected readonly selected = signal<Culture | null>(null);
  protected readonly feedings = signal<CultureFeeding[]>([]);

  protected readonly addOpen = signal(false);
  protected readonly splitOpen = signal(false);
  protected readonly editOpen = signal(false);

  protected readonly healthClass = CULTURE_HEALTH_CLASS;
  protected readonly kinds = CULTURE_KINDS;
  protected readonly healths = CULTURE_HEALTHS;
  protected readonly humanise = humanise;
  protected readonly relativeDays = relativeDays;

  // Add form
  protected newName = '';
  protected newFermentTypeId = '';
  protected newKind: CultureKind = 'SCOBY';
  protected newHealth: CultureHealth = 'HEALTHY';
  protected newFeedInterval: number | null = null;
  protected newFlavour = '';
  protected newNotes = '';

  // Split form
  protected splitName = '';
  protected splitNotes = '';

  // Edit form. Lineage and ferment style are immutable, so only the mutable fields are offered.
  protected readonly editing = signal<Culture | null>(null);
  protected editName = '';
  protected editHealth: CultureHealth = 'HEALTHY';
  protected editFeedInterval: number | null = null;
  protected editFlavour = '';
  protected editNotes = '';
  protected editActive = true;

  /** Cultures whose next feed is today or overdue. */
  protected readonly feedDue = computed(() => this.cultures().filter((culture) => culture.feedDue));

  /**
   * Family trees as an ECharts tree, which is the clearest way to show lineage across generations.
   */
  protected readonly treeOption = computed<EChartsOption>(() => ({
    tooltip: { trigger: 'item', triggerOn: 'mousemove', confine: true },
    series: this.tree().map((root, index) => ({
      type: 'tree',
      name: root.name,
      data: [this.toTreeData(root)],
      top: `${(index * 100) / Math.max(1, this.tree().length)}%`,
      height: `${88 / Math.max(1, this.tree().length)}%`,
      left: '8%',
      right: '22%',
      symbolSize: 9,
      orient: 'LR',
      itemStyle: { color: CHART_PALETTE[index % CHART_PALETTE.length] },
      label: {
        position: 'left',
        verticalAlign: 'middle',
        align: 'right',
        color: CHART_TEXT_COLOR,
        fontSize: 11
      },
      leaves: {
        label: { position: 'right', verticalAlign: 'middle', align: 'left' }
      },
      lineStyle: { color: '#44403c', width: 1 },
      expandAndCollapse: false,
      initialTreeDepth: -1
    }))
  }));

  /** Tree height scales with the number of roots so nothing overlaps. */
  protected readonly treeHeight = computed(() => Math.max(240, this.tree().length * 130));

  constructor() {
    this.fermentTypeService.findAll().subscribe((types) => this.fermentTypes.set(types));
    this.load();
  }

  protected select(culture: Culture): void {
    this.selected.set(culture);
    this.cultureService.findFeedings(culture.id).subscribe((feedings) => this.feedings.set(feedings));
  }

  protected closeDetail(): void {
    this.selected.set(null);
    this.feedings.set([]);
  }

  protected feed(culture: Culture): void {
    this.busy.set(true);
    this.cultureService.feed(culture.id, {}).subscribe({
      next: () => {
        this.toastService.success(`${culture.name} fed.`);
        this.load();
        if (this.selected()?.id === culture.id) {
          this.cultureService.findFeedings(culture.id).subscribe((feedings) => this.feedings.set(feedings));
        }
      },
      error: () => this.busy.set(false)
    });
  }

  protected openSplit(culture: Culture): void {
    this.selected.set(culture);
    this.splitName = `${culture.name} (gen ${culture.generation + 1})`;
    this.splitNotes = '';
    this.splitOpen.set(true);
  }

  protected openEdit(culture: Culture): void {
    this.editing.set(culture);
    this.editName = culture.name;
    this.editHealth = culture.health;
    this.editFeedInterval = culture.feedIntervalDays ?? null;
    this.editFlavour = culture.flavorProfile ?? '';
    this.editNotes = culture.notes ?? '';
    this.editActive = culture.active;
    this.editOpen.set(true);
  }

  protected saveEdit(): void {
    const culture = this.editing();
    if (!culture || !this.editName.trim()) {
      this.toastService.error('A name is required.');
      return;
    }

    this.busy.set(true);
    this.cultureService
      .update(culture.id, {
        name: this.editName.trim(),
        health: this.editHealth,
        feedIntervalDays: this.editFeedInterval,
        flavorProfile: this.editFlavour.trim() || null,
        notes: this.editNotes.trim() || null,
        active: this.editActive
      })
      .subscribe({
        next: (updated) => {
          this.editOpen.set(false);
          this.editing.set(null);
          this.toastService.success(`${updated.name} updated.`);
          this.load();
        },
        error: () => this.busy.set(false)
      });
  }

  protected remove(culture: Culture): void {
    this.busy.set(true);
    this.cultureService.delete(culture.id).subscribe({
      next: () => {
        this.toastService.success(`${culture.name} deleted.`);
        // Close the care log if the deleted culture was open there.
        if (this.selected()?.id === culture.id) {
          this.closeDetail();
        }
        this.load();
      },
      // A culture with descendants can't be deleted; the toast surfaces that business rule.
      error: () => this.busy.set(false)
    });
  }

  protected split(): void {
    const parent = this.selected();
    if (!parent || !this.splitName.trim()) {
      return;
    }

    this.busy.set(true);
    this.cultureService
      .split(parent.id, { name: this.splitName.trim(), notes: this.splitNotes.trim() || null })
      .subscribe({
        next: (child) => {
          this.splitOpen.set(false);
          this.toastService.success(`${child.name} split off ${parent.name}.`);
          this.load();
        },
        error: () => this.busy.set(false)
      });
  }

  protected add(): void {
    if (!this.newName.trim() || !this.newFermentTypeId) {
      this.toastService.error('A name and a ferment style are required.');
      return;
    }

    this.busy.set(true);
    this.cultureService
      .add({
        name: this.newName.trim(),
        fermentTypeId: this.newFermentTypeId,
        kind: this.newKind,
        health: this.newHealth,
        feedIntervalDays: this.newFeedInterval,
        flavorProfile: this.newFlavour.trim() || null,
        notes: this.newNotes.trim() || null
      })
      .subscribe({
        next: (culture) => {
          this.addOpen.set(false);
          this.resetAddForm();
          this.toastService.success(`${culture.name} added to the library.`);
          this.load();
        },
        error: () => this.busy.set(false)
      });
  }

  protected retire(culture: Culture): void {
    this.busy.set(true);
    this.cultureService
      .update(culture.id, {
        name: culture.name,
        health: culture.health,
        feedIntervalDays: culture.feedIntervalDays ?? null,
        flavorProfile: culture.flavorProfile ?? null,
        notes: culture.notes ?? null,
        active: !culture.active
      })
      .subscribe({
        next: (updated) => {
          this.toastService.success(updated.active ? `${updated.name} is active again.` : `${updated.name} retired.`);
          this.load();
        },
        error: () => this.busy.set(false)
      });
  }

  private resetAddForm(): void {
    this.newName = '';
    this.newFermentTypeId = '';
    this.newKind = 'SCOBY';
    this.newHealth = 'HEALTHY';
    this.newFeedInterval = null;
    this.newFlavour = '';
    this.newNotes = '';
  }

  private load(): void {
    this.loading.set(true);
    this.cultureService.findAll().subscribe({
      next: (cultures) => {
        this.cultures.set(cultures);
        this.loading.set(false);
        this.busy.set(false);

        // Keep the open detail panel in sync after a feed or a split.
        const open = this.selected();
        if (open) {
          this.selected.set(cultures.find((culture) => culture.id === open.id) ?? null);
        }
      },
      error: () => {
        this.loading.set(false);
        this.busy.set(false);
      }
    });

    this.cultureService.findTree().subscribe((tree) => this.tree.set(tree));
  }

  private toTreeData(node: CultureTreeNode): Record<string, unknown> {
    return {
      name: `${node.name} · g${node.generation}`,
      value: node.flavorProfile ?? humanise(node.kind),
      children: node.children.map((child) => this.toTreeData(child))
    };
  }
}
