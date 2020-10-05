import { NgModule } from "@angular/core";
import { EtutorPlusPlusSharedModule } from "../../shared/shared.module";
import { learningGoalsRoute } from "./learning-goals.route";
import { RouterModule } from "@angular/router";
import { LearningGoalsComponent } from "./learning-goals.component";
import { TreeviewModule } from "ngx-treeview";

/**
 * Module for the learning goal related components.
 */
@NgModule({
  imports: [EtutorPlusPlusSharedModule, RouterModule.forChild(learningGoalsRoute), TreeviewModule.forRoot()],
  declarations: [
    LearningGoalsComponent
  ]
})
export class LearningGoalsModule { }
