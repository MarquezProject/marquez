# Marquez Web UI

### Requirements

To develop on this project, you need to have Node version 10.16.3 installed. In order to easily switch between node versions, we recommend using node version manager like [nvm](https://github.com/nvm-sh/nvm/blob/master/README.md)

### Development

1. Install dependencies:

   ```
   $ npm install
   ```

2. Run webpack (with development server):

   ```
   $ npm run dev
   ```

### Testing

1. Run tests in watch mode:

   ```
   $ npm run test-watch
   ```

2. Run all tests:

   ```
   $ npm run test
   ```

### Testing Setup

- [Jest](https://jestjs.io/en/) is our testing framework. It is the test runner; it provides testing language (describe, it, beforeEach), mocking, snapshot, code coverage.
- [Enzyme](https://github.com/airbnb/enzyme) - testing utility for testing React components. Provides methods for (mock) rendering components & DOM traversal.
- run tests by doing `yarn run test`
- config files:
  - jest.config.js
  - setupEnzyme.ts
- For testing Redux Sagas, we are using `Redux Saga Test Plan`. This library gives us both unit testing and integration test functionality. Check out the [docs](http://redux-saga-test-plan.jeremyfairbank.com/).

### Typescript

This project is written in typescript. See [tsconfig.json](tsconfig.json) for our setup.

Our types are defined in the `src/types` folder

#### Linting & Prettifying

Fix all style issues in project
```
   $ npm run eslint-fix 
```

### Seeding data

Mock data can be found in the `docker/db/data` folder.
Feel free to edit the mock data if you like. (Grant worked really hard on making it pretty.)

#### Customization

We encourage you to make Marquez your own! Feel free to change styles, add new components, etc.

To get feedback on your customized version of Marquez, update the **FEEDBACK_FORM_URL** variable in the "plugins" section of `./webpack.prod.js`

### Contributing

See [CONTRIBUTING.md](https://github.com/MarquezProject/marquez-web/blob/master/CONTRIBUTING.md) for more details about how to contribute.
