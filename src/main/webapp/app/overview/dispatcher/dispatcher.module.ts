/**
 * Module for the dispatcher component.
 */
import { SharedModule } from 'app/shared/shared.module';
import { NgModule } from '@angular/core';
import { MonacoEditorModule, NgxMonacoEditorConfig } from 'ngx-monaco-editor';
import { AssignmentComponent } from 'app/overview/dispatcher/assignment/assignment.component';
import { LecturerRunSubmissionComponent } from './lecturer-run-submission/lecturer-run-submission.component';
import { SafeHtmlPipe } from './assignment/safe-html-pipe';

export function myMonacoLoad(): void {
  (window as any).monaco.languages.register({ id: 'relationalAlgebra' });

  // Register a tokens provider for the language
  (window as any).monaco.languages.setMonarchTokensProvider('relationalAlgebra', {
    tokenizer: {
      root: [
        [/PROJECTION/, 'projection'],
        [/SELECTION/, 'selection'],
        [/RENAMING/, 'renaming'],
        [/INTERSECTION/, 'intersection'],
        [/CARTESIAN_PRODUCT/, 'cartesian-product'],
        [/DIVISION/, 'division'],
        [/JOIN/, 'join'],
        [/LEFT/, 'left'],
        [/RIGHT/, 'right'],
        [/SEMI/, 'semi'],
        [/FULL/, 'full'],
        [/OUTER/, 'outer'],
        [/MINUS/, 'minus'],
        [/THETA/, 'theta'],
        [/UNION/, 'union'],
        [/\]/, 'right-bracket'],
        [/\[/, 'left-bracket'],
        [/\(/, 'right-parantheses'],
        [/\)/, 'right-parantheses'],
      ],
    },
  });

  const keyWordColor = '3a92bb';
  (window as any).monaco.editor.defineTheme('relationalAlgebra-light', {
    base: 'vs',
    inherit: false,
    rules: [
      { token: 'projection', foreground: keyWordColor },
      { token: 'selection', foreground: keyWordColor },
      { token: 'renaming', foreground: keyWordColor },
      { token: 'intersection', foreground: keyWordColor },
      { token: 'cartesian_product', foreground: keyWordColor },
      { token: 'division', foreground: keyWordColor },
      { token: 'join', foreground: keyWordColor },
      { token: 'left', foreground: keyWordColor },
      { token: 'right', foreground: keyWordColor },
      { token: 'semi', foreground: keyWordColor },
      { token: 'full', foreground: keyWordColor },
      { token: 'minus', foreground: keyWordColor },
      { token: 'theta', foreground: keyWordColor },
      { token: 'union', foreground: keyWordColor },
      { token: 'outer', foreground: keyWordColor },
      { token: 'left-bracket', foreground: 'f15530' },
      { token: 'right-bracket', foreground: 'f15530' },
      { token: 'left-parantheses', foreground: '6d28b1' },
      { token: 'right-parantheses', foreground: '6d28b1' },
    ],
  });

  (window as any).monaco.languages.registerCompletionItemProvider('relationalAlgebra', {
    provideCompletionItems: () => [
      {
        label: 'SELECTION',
        kind: (window as any).monaco.languages.CompletionItemKind.Text,
      },
      {
        label: 'PROJECTION',
        kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
        insertText: {
          value: 'PROJECTION(${1:condition})',
        },
      },
    ],
  });
}

const monacoConfig: NgxMonacoEditorConfig = {
  onMonacoLoad: myMonacoLoad,
};

@NgModule({
  imports: [SharedModule, MonacoEditorModule.forRoot(monacoConfig)],
  declarations: [AssignmentComponent, LecturerRunSubmissionComponent, SafeHtmlPipe],
  exports: [AssignmentComponent],
})
export class DispatcherModule {}
