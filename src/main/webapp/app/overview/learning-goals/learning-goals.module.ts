import { NgModule } from "@angular/core";
import { EtutorPlusPlusSharedModule } from "../../shared/shared.module";
import { learningGoalsRoute } from "./learning-goals.route";
import { RouterModule } from "@angular/router";
import { LearningGoalsComponent } from "./learning-goals.component";
import { TreeviewModule } from "ngx-treeview";
import {ContextMenuModule} from "ngx-contextmenu";

/**
 * Module for the learning goal related components.
 */
@NgModule({
  imports: [EtutorPlusPlusSharedModule, RouterModule.forChild(learningGoalsRoute), TreeviewModule.forRoot(), ContextMenuModule],
  declarations: [
    LearningGoalsComponent
  ]
})
export class LearningGoalsModule { }
