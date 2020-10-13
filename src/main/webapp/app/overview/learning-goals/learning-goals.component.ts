import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { LearningGoalsService } from './learning-goals.service';
import { LearningGoalTreeviewItem } from './learning-goal-treeview-item.model';
import { TreeviewComponent, TreeviewConfig } from 'ngx-treeview';
import { ContextMenuComponent } from 'ngx-contextmenu';
import * as d3 from 'd3';

/**
 * Component which is used for visualising the learning goals management.
 */
@Component({
  selector: 'jhi-learning-goals',
  templateUrl: './learning-goals.component.html',
  styleUrls: ['./learning-goals.component.scss'],
})
export class LearningGoalsComponent implements OnInit {
  @ViewChild(TreeviewComponent, { static: false })
  public treeviewComponent?: TreeviewComponent;
  @ViewChild('learningGoalCtxMenu')
  public learningGoalCtxMenu?: ContextMenuComponent;

  @ViewChild('graph')
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

  private data(): any {
    return {};
  }

  private drawGraph(): void {
    const margin = { top: 10, right: 30, bottom: 30, left: 40 },
      width = 400 - margin.left - margin.right,
      height = 400 - margin.top - margin.bottom;

    // append the svg object to the body of the page
    const svg = d3
      .select('#graph')
      .append('svg')
      .attr('width', width + margin.left + margin.right)
      .attr('height', height + margin.top + margin.bottom)
      .append('g')
      .attr('transform', 'translate(' + margin.left + ',' + margin.top + ')');

    d3.json<any>('https://raw.githubusercontent.com/holtzy/D3-graph-gallery/master/DATA/data_network.json')
      .then(data => {
        console.log(data);

        // Initialize the links
        const link = svg.selectAll('line').data(data.links).enter().append('line').style('stroke', '#aaa');

        // Initialize the nodes
        const node = svg.selectAll('circle').data(data.nodes).enter().append('circle').attr('r', 20).style('fill', '#69b3a2');

        // Let's list the force we wanna apply on the network
        d3.forceSimulation(data.nodes) // Force algorithm is applied to data.nodes
          .force(
            'link',
            d3
              .forceLink() // This force provides links between nodes
              .id((d: any) => d.id) // This provide the id of a node
              .links(data.links) // and this the list of links
          )
          .force('charge', d3.forceManyBody().strength(-400)) // This adds repulsion between nodes. Play with the -400 for the repulsion strength
          .force('center', d3.forceCenter(width / 2, height / 2)) // This force attracts nodes to the center of the svg area
          .on('end', () => {
            link
              .attr('x1', (d: any) => {
                return d.source.x;
              })
              .attr('y1', (d: any) => {
                return d.source.y;
              })
              .attr('x2', (d: any) => {
                return d.target.x;
              })
              .attr('y2', (d: any) => {
                return d.target.y;
              });

            node
              .attr('cx', (d: any) => {
                return d.x + 6;
              })
              .attr('cy', (d: any) => {
                return d.y - 6;
              });
          });
      })
      .catch(err => {
        alert('ERROR loading data' + err);
      });
  }

  /**
   * Implements the on init method. See {@link OnInit}
   */

  public ngOnInit(): void {
    this.loadLearningGoalsAsync();
    this.drawGraph();
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
