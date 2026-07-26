export default function mockSignIn(params: { lessonId: string; studentIds: string[] }): { success: boolean } {
  console.log('[Mock] signIn:', params);
  
  return { success: true };
}
