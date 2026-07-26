export default function mockCompleteCourse(params: { lessonId: string }): { success: boolean } {
  console.log('[Mock] completeCourse:', params);
  
  return { success: true };
}
