module.exports = {
  roots: ['<rootDir>/src'],
  transform: {
    '^.+\\.tsx?$': 'ts-jest'
  },
  testRegex: '__tests__/(.+).(test|spec).tsx?',
  testPathIgnorePatterns: ['<rootDir>/src/__tests__(.+)__snapshots__'],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node'],
  snapshotSerializers: ['enzyme-to-json/serializer'],
  setupTestFrameworkScriptFile: '<rootDir>/setupEnzyme.ts',
  globalSetup: '<rootDir>globalSetup.ts',
  setupFiles: ['<rootDir>setupJest.ts'],
  globals: {
    'ts-jest': {
      isolatedModules: 'true'
    },
    __API_URL__: '/api/v1',
    __FEEDBACK_FORM_URL__: 'https://forms.gle/f3tTSrZ8wPj3sHTA7',
    __API_DOCS_URL__: 'https://marquezproject.github.io/marquez/openapi.html',
    __TEMP_ACTOR_STR__: 'me'
  },
  moduleNameMapper: {
    '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$':
      '<rootDir>/__tests__/__mocks__/fileMock.js',
    '\\.(css|less)$': 'identity-obj-proxy'
  }
}
