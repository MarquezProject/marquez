module.exports = {
  roots: ['<rootDir>/src'],
  transform: {
    '^.+\\.tsx?$': [
      'ts-jest',
      {
        isolatedModules: 'true'
      }
    ]
  },
  testRegex: '__tests__/(.+).(test|spec).tsx?',
  testPathIgnorePatterns: ['<rootDir>/src/__tests__(.+)__snapshots__'],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node'],
  globalSetup: '<rootDir>globalSetup.ts',
  setupFiles: ['<rootDir>setupJest.ts'],
  testEnvironment: 'jsdom',
  globals: {
    __API_URL__: '/api/v1',
    __API_BETA_URL__: '/api/v2beta',
    __FEEDBACK_FORM_URL__: 'https://forms.gle/f3tTSrZ8wPj3sHTA7',
    __REACT_APP_ADVANCED_SEARCH__: true,
    __API_DOCS_URL__: 'https://marquezproject.github.io/marquez/openapi.html',
    __TEMP_ACTOR_STR__: 'me'
  },
  moduleNameMapper: {
    '\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$':
      '<rootDir>/__tests__/__mocks__/fileMock.js',
    '\\.(css|less)$': 'identity-obj-proxy',
    "d3": "<rootDir>/node_modules/d3/dist/d3.min.js",
    "^d3-(.*)$": "<rootDir>/node_modules/d3-$1/dist/d3-$1.min.js"
  }
}
