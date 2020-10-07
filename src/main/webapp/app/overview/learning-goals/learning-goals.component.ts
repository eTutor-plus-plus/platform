import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { LearningGoalsService } from './learning-goals.service';
import { LearningGoalTreeviewItem } from './learning-goal-treeview-item.model';
import { TreeviewComponent, TreeviewConfig } from 'ngx-treeview';
import { ContextMenuComponent } from 'ngx-contextmenu';
import * as cytoscape from 'cytoscape';
import Any = jasmine.Any;

/**
 * Component which is used for visualising the learning goals management.
 */
@Component({
  selector: 'jhi-learning-goals',
  templateUrl: './learning-goals.component.html',
  styleUrls: ['./learning-goals.component.scss'],
})
export class LearningGoalsComponent implements OnInit, AfterViewInit {
  @ViewChild(TreeviewComponent, { static: false })
  public treeviewComponent?: TreeviewComponent;
  @ViewChild('learningGoalCtxMenu')
  public learningGoalCtxMenu?: ContextMenuComponent;

  @ViewChild('cy')
  public graph?: ElementRef;

  public learningGoals: LearningGoalTreeviewItem[] = [];
  public selectedLearningGoal?: LearningGoalTreeviewItem;
  public config = TreeviewConfig.create({
    hasAllCheckBox: false,
    hasFilter: true,
    hasCollapseExpand: true,
  });

  /**
   * Constructor
   *
   * @param learningGoalsService the injected learning goals service
   */

  constructor(private learningGoalsService: LearningGoalsService) {}

  ngAfterViewInit(): void {
    this.initCytoscape();
  }

  /**
   * Implements the on init method. See {@link OnInit}
   */

  public displayGraph(): void {
    this.initCytoscape();
  }

  public initCytoscape(): void {
    if (this.graph) {
      this.graph.nativeElement.className = 'cy';
      cytoscape({
        container: this.graph.nativeElement,
        zoomingEnabled: false,
        boxSelectionEnabled: false,
        elements: [
          {
            data: { id: 'A' },
          },
          {
            data: { id: 'B' },
          },
          {
            data: { id: 'C' },
          },
          { data: { id: 'ab', source: 'A', target: 'B' } },
          { data: { id: 'ac', source: 'A', target: 'C' } },
          { data: { id: 'bc', source: 'B', target: 'C' } },
        ],

        style: [
          {
            selector: 'node',
            style: {
              'background-color': '#666',
              label: 'data(id)',
            },
          },
          {
            selector: 'edge',
            style: {
              width: 10,
              'line-color': '#ccc',
              'target-arrow-color': '#ccc',
              'target-arrow-shape': 'diamond',
            },
          },
        ],
        layout: {
          name: 'grid',
          rows: 5,
        },
      });
    }
  }

  public ngOnInit(): void {
    this.loadLearningGoalsAsync();
  }

  /**
   * Loads all visible learning goals as tree view items asynchronously.
   */
  private async loadLearningGoalsAsync(): Promise<void> {
    this.learningGoals.length = 0;
    const list = await this.learningGoalsService.getAllVisibleLearningGoalsAsTreeViewItems().toPromise();
    list.forEach(x => this.learningGoals.push(x));
  }

  /**
   * Event handler which handles selected learning goal items.
   *
   * @param item the selected learning goal
   */
  public onSelect(item: LearningGoalTreeviewItem): void {
    this.selectedLearningGoal = item;
  }

  /**
   * Event handler which handles the create new sub goal event.
   *
   * @param parent the parent treeview item of the sub goal which should be created
   */
  public onCreateSubGoal(parent: LearningGoalTreeviewItem): void {
    // eslint-disable-next-line no-console
    console.log('onCreateSubGoal for ' + JSON.stringify(parent));
  }
}
