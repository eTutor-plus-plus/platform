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
        [/LEFTARROW/, 'leftarrow'],
        [/\]/, 'right-bracket'],
        [/\[/, 'left-bracket'],
        [/\(/, 'left-parantheses'],
        [/\)/, 'right-parantheses'],
        [/\}/, 'right-curly'],
        [/\{/, 'left-curly'],
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
      { token: 'leftarrow', foreground: keyWordColor },
      { token: 'left-bracket', foreground: 'f15530' },
      { token: 'right-bracket', foreground: 'f15530' },
      { token: 'left-parantheses', foreground: '6d28b1' },
      { token: 'right-parantheses', foreground: '6d28b1' },
      { token: 'right-curly', foreground: '2d7c02' },
      { token: 'left-curly', foreground: '2d7c02' },
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
            'WHERE NOT EXISTS {',
            '\t SELECT ${3:*} ',
            '\t FROM ${4:relation}',
            '\t WHERE NOT EXISTS {',
            '\t \t SELECT ${5:*} ',
            '\t \t FROM ${6:relation}',
            '\t \t WHERE ${7:condition}',
            '\t }',
            '}',
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
}
