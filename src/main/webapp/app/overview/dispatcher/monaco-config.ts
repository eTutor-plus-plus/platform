/**
 * Export Function that can be used as {@see onMonacoLoad()} function for the Monaco editor configuration and
 * registers custom languages, themes and token-providers for the Monaco text editor,
 * currently for Relational Algebra, XQuery and Datalog
 */
import { TaskAssignmentType } from '../tasks/task.model';
import { NgxMonacoEditorConfig } from 'ngx-monaco-editor-v2';

export const monacoConfig: NgxMonacoEditorConfig = {
  baseUrl: 'assets',
  defaultOptions: { scrollBeyondLastLine: false },
  onMonacoLoad: myMonacoLoad,
};

export function myMonacoLoad(): void {
  console.log((window as any).monaco);
  console.log('Hello');

  // Register a tokens provider for the language
  (window as any).monaco.languages.register({ id: 'datalog' });
  (window as any).monaco.languages.setMonarchTokensProvider('datalog', {
    tokenizer: {
      root: [
        [/\./, 'dot'],
        [/:-/, 'assignment'],
        [/\(/, 'left-parantheses'],
        [/\)/, 'right-parantheses'],
        [/\?/, 'question-mark'],
      ],
    },
  });

  // Register a tokens provider for the language
  (window as any).monaco.languages.register({ id: 'relationalAlgebra' });
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
        [/LEFTARROW/, 'leftarrow'],
        [/LEFT/, 'left'],
        [/RIGHT/, 'right'],
        [/SEMI/, 'semi'],
        [/FULL/, 'full'],
        [/OUTER/, 'outer'],
        [/MINUS/, 'minus'],
        [/THETA/, 'theta'],
        [/UNION/, 'union'],
        [/{([^}]+)}/, 'theta-join'],
        [/\[.*?\]/, 'between-brackets'],
        [/\]/, 'right-bracket'],
        [/\[/, 'left-bracket'],
        [/\(/, 'left-parantheses'],
        [/\)/, 'right-parantheses'],
        [/\}/, 'right-curly'],
        [/\{/, 'left-curly'],
      ],
    },
  });

  // Register a tokens provider for the language
  (window as any).monaco.languages.register({ id: 'xquery' });
  (window as any).monaco.languages.setMonarchTokensProvider('xquery', {
    tokenizer: {
      root: [
        [/<\/[^>]+>/, 'closing-tag'], // Matches closing tags e.g. </company>
        [/<[^\/>]+>/, 'opening-tag'], // Matches opening tags e.g. <company>
        [/\/node\(\)/, 'node'],
        [/(^|[^a-zA-Z])for(?![a-zA-Z])/, 'for'],
        [/(^|[^a-zA-Z])let(?![a-zA-Z])/, 'let'],
        [/(^|[^a-zA-Z])where(?![a-zA-Z])/, 'where'],
        [/(^|[^a-zA-Z])order by(?![a-zA-Z])/, 'order-by'],
        [/(^|[^a-zA-Z])return|returns(?![a-zA-Z])/, 'return'],
        [/(^|[^a-zA-Z])if(?![a-zA-Z])/, 'if'],
        [/(^|[^a-zA-Z])in(?![a-zA-Z])/, 'in'],
        [/(^|[^a-zA-Z])then(?![a-zA-Z])/, 'then'],
        [/(^|[^a-zA-Z])else(?![a-zA-Z])/, 'else'],
        [/(^|[^a-zA-Z])every(?![a-zA-Z])/, 'every'],
        [/(^|[^a-zA-Z])some(?![a-zA-Z])/, 'some'],
        [/(^|[^a-zA-Z])satisfies/, 'satisfies'],
        [/define function/, 'define-function'],
        [/\$[a-z]+(?![a-zA-Z])/, 'variable'],
        [/(^|[^a-zA-Z])count(?![a-zA-Z])/, 'count'],
        [/(^|[^a-zA-Z])last(?![a-zA-Z])/, 'last'],
        [/(^|[^a-zA-Z])empty(?![a-zA-Z])/, 'empty'],
        [/(^|[^a-zA-Z])avg(?![a-zA-Z])/, 'avg'],
        [/(^|[^a-zA-Z])max(?![a-zA-Z])/, 'max'],
        [/(^|[^a-zA-Z])min(?![a-zA-Z])/, 'min'],
        [/(^|[^a-zA-Z])sum(?![a-zA-Z])/, 'sum'],
        [/(^|[^a-zA-Z])not(?![a-zA-Z])/, 'not'],
        [/(^|[^a-zA-Z])substring(?![a-zA-Z])/, 'substring'],
        [/(^|[^a-zA-Z])distinct-nodes(?![a-zA-Z])/, 'distinct-nodes'],
        [/(^|[^a-zA-Z])distinct-values(?![a-zA-Z])/, 'distinct-values'],
        [/(^|[^a-zA-Z])node-name(?![a-zA-Z])/, 'node-name'],
        [/(^|[^a-zA-Z])data(?![a-zA-Z])/, 'data'],
        [/(^|[^a-zA-Z])doc(?![a-zA-Z])/, 'doc'],
        [/(^|[^a-zA-Z])and(?![a-zA-Z])/, 'and'],
        [/\sor\s(?![a-zA-Z])/, 'or'],
        [/'.+'/, 'string'],
        [/:=/, 'assignment-sign'],
        [/\]/, 'right-bracket'],
        [/\[/, 'left-bracket'],
        [/\(/, 'left-parantheses'],
        [/\)/, 'right-parantheses'],
        [/\}/, 'right-curly'],
        [/\{/, 'left-curly'],
      ],
    },
  });

  let keyWordColor = 'EE4B2B';
  (window as any).monaco.editor.defineTheme('datalog-light', {
    colors: {},
    base: 'vs',
    inherit: true,
    rules: [
      { token: 'dot', foreground: '00FF00', fontStyle: 'bold' },
      { token: 'assignment', foreground: keyWordColor, fontStyle: 'bold' },
      { token: 'left-parantheses', foreground: keyWordColor, fontStyle: 'bold' },
      { token: 'right-parantheses', foreground: keyWordColor, fontStyle: 'bold' },
      { token: 'question-mark', foreground: '00FF00', fontStyle: 'bold' },
    ],
  });
  (window as any).monaco.editor.defineTheme('datalog-dark', {
    colors: {},
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'dot', foreground: '00FF00', fontStyle: 'bold' },
      { token: 'assignment', foreground: keyWordColor, fontStyle: 'bold' },
      { token: 'left-parantheses', foreground: keyWordColor, fontStyle: 'bold' },
      { token: 'right-parantheses', foreground: keyWordColor, fontStyle: 'bold' },
      { token: 'question-mark', foreground: '00FF00', fontStyle: 'bold' },
    ],
  });

  keyWordColor = '3a92bb';
  (window as any).monaco.editor.defineTheme('relationalAlgebra-light', {
    colors: {},
    base: 'vs',
    inherit: true,
    rules: [
      { token: 'projection', foreground: keyWordColor },
      { token: 'selection', foreground: keyWordColor },
      { token: 'renaming', foreground: keyWordColor },
      { token: 'intersection', foreground: keyWordColor },
      { token: 'cartesian-product', foreground: keyWordColor },
      { token: 'division', foreground: keyWordColor },
      { token: 'join', foreground: keyWordColor },
      { token: 'right', foreground: keyWordColor },
      { token: 'semi', foreground: keyWordColor },
      { token: 'full', foreground: keyWordColor },
      { token: 'minus', foreground: keyWordColor },
      { token: 'theta', foreground: keyWordColor },
      { token: 'union', foreground: keyWordColor },
      { token: 'outer', foreground: keyWordColor },
      { token: 'between-brackets', foreground: 'f15530', fontStyle: 'italic' },
      { token: 'leftarrow', foreground: keyWordColor },
      { token: 'left', foreground: keyWordColor },
      { token: 'theta-join', foreground: '0FB695' },
      { token: 'right-curly', foreground: '2d7c02' },
      { token: 'left-curly', foreground: '2d7c02' },
    ],
  });
  (window as any).monaco.editor.defineTheme('relationalAlgebra-dark', {
    colors: {},
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'projection', foreground: keyWordColor },
      { token: 'selection', foreground: keyWordColor },
      { token: 'renaming', foreground: keyWordColor },
      { token: 'intersection', foreground: keyWordColor },
      { token: 'cartesian-product', foreground: keyWordColor },
      { token: 'division', foreground: keyWordColor },
      { token: 'join', foreground: keyWordColor },
      { token: 'right', foreground: keyWordColor },
      { token: 'semi', foreground: keyWordColor },
      { token: 'full', foreground: keyWordColor },
      { token: 'minus', foreground: keyWordColor },
      { token: 'theta', foreground: keyWordColor },
      { token: 'union', foreground: keyWordColor },
      { token: 'outer', foreground: keyWordColor },
      { token: 'leftarrow', foreground: keyWordColor },
      { token: 'between-brackets', foreground: 'f15530', fontStyle: 'italic' },
      { token: 'left', foreground: keyWordColor },
      { token: 'theta-join', foreground: '0FB695' },
      { token: 'right-curly', foreground: '2d7c02' },
      { token: 'left-curly', foreground: '2d7c02' },
    ],
  });
  keyWordColor = 'F12F2F';
  (window as any).monaco.editor.defineTheme('xquery-light', {
    colors: {},
    base: 'vs',
    inherit: true,
    rules: [
      { token: 'opening-tag', foreground: '#3a92bb', fontStyle: 'bold' },
      { token: 'closing-tag', foreground: '#3a92bb', fontStyle: 'bold' },
      { token: 'node', foreground: keyWordColor },
      { token: 'for', foreground: keyWordColor },
      { token: 'in', foreground: keyWordColor },
      { token: 'let', foreground: keyWordColor },
      { token: 'where', foreground: keyWordColor },
      { token: 'order-by', foreground: keyWordColor },
      { token: 'return', foreground: keyWordColor },
      { token: 'if', foreground: keyWordColor },
      { token: 'then', foreground: keyWordColor },
      { token: 'else', foreground: keyWordColor },
      { token: 'every', foreground: keyWordColor },
      { token: 'some', foreground: keyWordColor },
      { token: 'satisfies', foreground: keyWordColor },
      { token: 'define-function', foreground: keyWordColor },
      { token: 'variable', foreground: '#7C57E8' },
      { token: 'count', foreground: keyWordColor },
      { token: 'last', foreground: keyWordColor },
      { token: 'empty', foreground: keyWordColor },
      { token: 'avg', foreground: keyWordColor },
      { token: 'min', foreground: keyWordColor },
      { token: 'max', foreground: keyWordColor },
      { token: 'sum', foreground: keyWordColor },
      { token: 'not', foreground: keyWordColor },
      { token: 'substring', foreground: keyWordColor },
      { token: 'distinct-nodes', foreground: keyWordColor },
      { token: 'distinct-values', foreground: keyWordColor },
      { token: 'node-name', foreground: keyWordColor },
      { token: 'data', foreground: keyWordColor },
      { token: 'doc', foreground: keyWordColor },
      { token: 'and', foreground: keyWordColor },
      { token: 'or', foreground: keyWordColor },
      { token: 'string', foreground: '#039307' },
      { token: 'left-parantheses', fontStyle: 'bold' },
      { token: 'right-parantheses', fontStyle: 'bold' },
      { token: 'right-curly', fontStyle: 'bold' },
      { token: 'left-curly', fontStyle: 'bold' },
      { token: 'assignment-sign', fontStyle: 'bold' },
    ],
  });

  (window as any).monaco.editor.defineTheme('xquery-dark', {
    colors: {},
    base: 'vs-dark',
    inherit: true,
    rules: [
      { token: 'opening-tag', foreground: '#3a92bb', fontStyle: 'bold' },
      { token: 'closing-tag', foreground: '#3a92bb', fontStyle: 'bold' },
      { token: 'in', foreground: keyWordColor },
      { token: 'node', foreground: keyWordColor },
      { token: 'for', foreground: keyWordColor },
      { token: 'let', foreground: keyWordColor },
      { token: 'where', foreground: keyWordColor },
      { token: 'order-by', foreground: keyWordColor },
      { token: 'return', foreground: keyWordColor },
      { token: 'if', foreground: keyWordColor },
      { token: 'then', foreground: keyWordColor },
      { token: 'else', foreground: keyWordColor },
      { token: 'every', foreground: keyWordColor },
      { token: 'some', foreground: keyWordColor },
      { token: 'satisfies', foreground: keyWordColor },
      { token: 'define-function', foreground: keyWordColor },
      { token: 'variable', foreground: '#7C57E8' },
      { token: 'count', foreground: keyWordColor },
      { token: 'last', foreground: keyWordColor },
      { token: 'empty', foreground: keyWordColor },
      { token: 'avg', foreground: keyWordColor },
      { token: 'min', foreground: keyWordColor },
      { token: 'max', foreground: keyWordColor },
      { token: 'sum', foreground: keyWordColor },
      { token: 'not', foreground: keyWordColor },
      { token: 'substring', foreground: keyWordColor },
      { token: 'distinct-nodes', foreground: keyWordColor },
      { token: 'distinct-values', foreground: keyWordColor },
      { token: 'node-name', foreground: keyWordColor },
      { token: 'data', foreground: keyWordColor },
      { token: 'doc', foreground: keyWordColor },
      { token: 'and', foreground: keyWordColor },
      { token: 'or', foreground: keyWordColor },
      { token: 'string', foreground: '#039307' },
      { token: 'left-parantheses', fontStyle: 'bold' },
      { token: 'right-parantheses', fontStyle: 'bold' },
      { token: 'right-curly', fontStyle: 'bold' },
      { token: 'left-curly', fontStyle: 'bold' },
      { token: 'assignment-sign', fontStyle: 'bold' },
    ],
  });
  (window as any).monaco.languages.registerCompletionItemProvider('relationalAlgebra', {
    provideCompletionItems: () => ({
      suggestions: [
        {
          label: 'PROJECTION',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'PROJECTION[${1:columns}](${2:relation})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'SELECTION',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'SELECTION[${1:predicate}](${2:relation})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'RENAMING',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'RENAMING[${1:columnName} LEFTARROW ${2:alias}](${3:relation})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'THETA-JOIN',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: '(${1:relation1}{${2:predicate}}${3:relation2})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'JOIN',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: '(${1:relation1} JOIN ${2:relation2})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'DIVISION',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: '(${1:relation1} DIVISION ${2:relation2})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'UNION',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: '(${1:relation1} UNION ${2:relation2})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'INTERSECTION',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: '(${1:relation1} INTERSECTION ${2:relation2})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'CARTESIAN-PRODUCT',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: '(${1:relation1} CARTESIAN_PRODUCT ${2:relation2})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
      ],
    }),
  });
  (window as any).monaco.languages.registerCompletionItemProvider('pgsql', {
    provideCompletionItems: () => ({
      suggestions: [
        {
          label: 'division-query',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: [
            'SELECT ${1:*} ',
            'FROM ${2:relation}',
            'WHERE NOT EXISTS (',
            '\t SELECT ${3:*} ',
            '\t FROM ${4:relation}',
            '\t WHERE NOT EXISTS (',
            '\t \t SELECT ${5:*} ',
            '\t \t FROM ${6:relation}',
            '\t \t WHERE ${7:condition}',
            '\t )',
            ')',
          ].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'simple-query',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: ['SELECT ${1:*} ', 'FROM ${2:relation}', 'WHERE ${3:condition}'].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'join-query',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: ['SELECT ${1:*} ', 'FROM ${2:relation1} JOIN ${3: relation2} ON ${4:join-predicate}'].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'union-query',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: ['SELECT ${1:*} ', 'FROM ${2:relation1}', '', 'UNION', '', 'SELECT ${3:*} ', 'FROM ${4:relation2}'].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'group-by-query',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: [
            'SELECT ${1:grouping-columns} ',
            'FROM ${2:relation1}',
            'GROUP BY ${1:grouping-columns}',
            'HAVING ${3:condition}',
          ].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
      ],
    }),
  });

  (window as any).monaco.languages.registerCompletionItemProvider('xquery', {
    provideCompletionItems: () => ({
      suggestions: [
        {
          label: 'if-then-else',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: ['if (${1:condition})', 'then ${2:consequent}', 'else ${3:alternative}'].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'every',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: ['every $${1:variable} in ${2:sequence}', 'satisfies ${3:expression}'].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'some',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: ['some $${1:variable} in ${2:sequence}', 'satisfies ${3:expression}'].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'for',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: ['for $${1:variable} in ${2:sequence}', 'return ${3:expression}'].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'variable-declaration',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'let $${1:variable} := ${2:expression}',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'doc',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: "let $${1:variable} := doc('${2:url}')",
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'position',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'position(${1:node})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'last',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'last(${1:sequence})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'count',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'count(${1:sequence})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'empty',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'empty(${1:sequence})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'avg',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'empty(${1:values})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'max',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'max(${1:values})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'min',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'min(${1:values})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'sum',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'sum(${1:values})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'not',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'not(${1:boolean})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'substring',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'substring(${1:string}, ${2:int})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'distinct-nodes',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'distinct-nodes(${1:nodes})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'distinct-values',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'distinct-values(${1:values})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'node-name',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'node-name(${1:node})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'data',
          kind: (window as any).monaco.languages.CompletionItemKind.Keyword,
          insertText: 'data(${1:node})',
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
        {
          label: 'function-declaration',
          kind: (window as any).monaco.languages.CompletionItemKind.Snippet,
          insertText: [
            'define function ${1:function-name}(${2:datatype} $${3:in-param}) returns ${4:return-datatype} {',
            '${5:body}',
            '}',
          ].join('\n'),
          insertTextRules: (window as any).monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        },
      ],
    }),
  });
  console.log((window as any).monaco);
  console.log('Hello');
}
export interface MonacoEditorOptions {
  theme: string;
  language: string;
  readOnly: boolean;
}

export function getEditorOptionsForTaskTypeUrl(taskTypeUrl: string, readOnly: boolean): MonacoEditorOptions {
  return {
    theme: getThemeForEditorOptionsForTaskTypeUrl(taskTypeUrl),
    language: getLanguageForEditorOptionsForTaskTypeUrl(taskTypeUrl),
    readOnly,
  };
}

export function switchModeOfEditorConfig(config: MonacoEditorOptions): MonacoEditorOptions {
  const newTheme = switchTheme(config.theme);
  return {
    ...config,
    theme: newTheme,
  };
}

function getLanguageForEditorOptionsForTaskTypeUrl(taskTypeUrl: string): string {
  switch (taskTypeUrl) {
    case TaskAssignmentType.SQLTask.value:
      return 'pgsql';
    case TaskAssignmentType.RATask.value:
      return 'relationalAlgebra';
    case TaskAssignmentType.DatalogTask.value:
      return 'datalog';
    case TaskAssignmentType.XQueryTask.value:
      return 'xquery';
    case TaskAssignmentType.BpmnTask.value:
      return 'bpmn';
    default:
      return 'pgsql';
  }
}

function getThemeForEditorOptionsForTaskTypeUrl(taskTypeUrl: string): string {
  switch (taskTypeUrl) {
    case TaskAssignmentType.SQLTask.value:
      return 'vs-light';
    case TaskAssignmentType.RATask.value:
      return 'relationalAlgebra-light';
    case TaskAssignmentType.DatalogTask.value:
      return 'datalog-light';
    case TaskAssignmentType.XQueryTask.value:
      return 'xquery-light';
    case TaskAssignmentType.BpmnTask.value:
      return 'xml';
    default:
      return 'vs-light';
  }
}
function switchTheme(currentTheme: string): string {
  switch (currentTheme) {
    case 'relationalAlgebra-light':
      return 'relationalAlgebra-dark';
    case 'relationalAlgebra-dark':
      return 'relationalAlgebra-light';
    case 'datalog-light':
      return 'datalog-dark';
    case 'datalog-dark':
      return 'datalog-light';
    case 'xquery-light':
      return 'xquery-dark';
    case 'xquery-dark':
      return 'xquery-light';
    case 'vs-light':
      return 'vs-dark';
    case 'vs-dark':
      return 'vs-light';
    case 'xml':
      return 'xml';
    default:
      return 'vs-light';
  }
}
