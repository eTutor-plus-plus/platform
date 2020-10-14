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
    return {
      nodes: [
        { id: 1, name: 'A' },
        { id: 2, name: 'B' },
        { id: 3, name: 'C' },
        { id: 4, name: 'D' },
        { id: 5, name: 'E' },
        { id: 6, name: 'F' },
        { id: 7, name: 'G' },
        { id: 8, name: 'H' },
      ],
      links: [
        { source: 1, target: 2 },
        { source: 1, target: 3 },
        { source: 2, target: 3, successor: true },
        { source: 2, target: 3 },
        { source: 2, target: 4 },
        { source: 2, target: 5 },
        { source: 3, target: 4, successor: true },
        { source: 4, target: 5, successor: true },
        { source: 5, target: 6, successor: true },
        { source: 3, target: 7 },
        { source: 3, target: 8 },
      ],
    };
  }

  private dataHierachy(): any {
    return {
      name: 'test 1',
      children: [
        {
          name: 'test 2',
          children: [
            {
              name: 'test 4',
            },
            {
              name: 'test 5',
              depends: ['test 4'],
            },
            {
              name: 'test 6',
            },
          ],
        },
        {
          name: 'test 3',
          children: [
            {
              name: 'test 7',
            },
            {
              name: 'test 8',
              depends: ['test 4'],
            },
          ],
        },
      ],
    };
  }

  private drawGraph(): void {
    const data = this.dataHierachy();
    const treeLayout = d3.tree().size([300, 200]);
    const root = d3.hierarchy(data);
    treeLayout(root);

    this.drawNodes(root);
    this.drawLabels(root);
    this.drawLinks(root);

    const edges: any = [];
    this.buildDependsEdges(root, edges);
    this.drawDependsOnEdges(edges);
  }

  private drawNodes(root: any): any {
    d3.select('svg g.nodes')
      .selectAll('circle.node')
      .data(root.descendants())
      .enter()
      .append('circle')
      .classed('node', true)
      .attr('cx', (d: any) => {
        return d.x;
      })
      .attr('cy', (d: any) => {
        return d.y;
      })
      .attr('r', 6)
      .attr('stroke-width', 1)
      .attr('stroke', 'black')
      .style('fill', 'white');
  }

  private drawLabels(root: any): void {
    d3.select('svg g.nodes')
      .selectAll('text.label')
      .data(root.descendants())
      .enter()
      .append('text')
      .classed('label', true)
      .attr('dx', (d: any) => {
        return d.x + 5;
      })
      .attr('dy', (d: any) => {
        return d.y - 5;
      })
      .text((d: any) => {
        return d.data.name;
      });
  }

  private drawLinks(root: any): void {
    d3.select('svg g.links')
      .selectAll('line.link')
      .data(root.links())
      .enter()
      .append('line')
      .classed('link', true)
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
      })
      .attr('style', 'fill: none;stroke: #ccc;stroke-width: 2px;');
  }

  private buildDependsEdges(root: any, edges: any): any {
    root.descendants().forEach((n: any) => {
      if (n.data.depends !== undefined) {
        n.data.depends.forEach((name: string) => {
          const other: any = root.descendants().find((x: any) => x.data.name === name);
          edges.push({
            source: {
              x: n.x,
              y: n.y,
            },
            target: {
              x: other.x,
              y: other.y,
            },
          });
        });
      }
    });
  }

  private drawDependsOnEdges(edges: any): void {
    d3.select('svg')
      .append('defs')
      .append('marker')
      .attr('id', 'Triangle')
      .attr('refX', 6 * 2)
      .attr('refY', 3 * 2)
      .attr('markerUnits', 'userSpaceOnUse')
      .attr('markerWidth', 12)
      .attr('markerHeight', 18)
      .attr('orient', '-25deg')
      .append('path')
      .style('fill', '#CC9999')
      .attr('d', 'M 0 0 12 6 0 12 3 6');

    d3.select('svg g.depends')
      .selectAll('path.depend')
      .data(edges)
      .enter()
      .append('path')
      .classed('depend', true)
      .attr('x2', (d: any) => {
        return d.source.x;
      })
      .attr('y2', (d: any) => {
        return d.source.y;
      })
      .attr('x1', (d: any) => {
        return d.target.x;
      })
      .attr('y1', (d: any) => {
        return d.target.y;
      })
      .attr('style', 'fill: none;stroke: #abcd;stroke-width: 2px;')
      .attr('d', (d: any) => {
        const dx = d.target.x - d.source.x,
          dy = d.target.y - d.source.y,
          dr = Math.sqrt(dx * dx + dy * dy);
        return 'M' + d.source.x + ',' + d.source.y + 'A' + dr + ',' + dr + ' 0 0,1 ' + d.target.x + ',' + d.target.y;
      })
      .attr('marker-start', 'url(#Triangle)');
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
