export function mapEditorOption(taskType: string): string {
  switch (taskType) {
    case 'http://www.dke.uni-linz.ac.at/etutorpp/TaskAssignmentType#SQLTask':
      return 'sql';
      break;
    default:
      return 'sql';
  }
}
