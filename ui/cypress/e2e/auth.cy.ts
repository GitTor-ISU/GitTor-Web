import { faker } from '@faker-js/faker';

const adminUsername = Cypress.env('ADMIN_USERNAME') || 'admin';
const adminEmail = Cypress.env('ADMIN_EMAIL') || 'admin@gittor';
const adminPassword = Cypress.env('ADMIN_PASSWORD') || 'password';

describe('Login', function () {
  it('should redirect to home page after admin login', () => {
    cy.login(adminUsername, adminPassword);
    cy.location('pathname').should('equal', '/');
    cy.verifyLogin().should('be.true');
  });

  it('should redirect off of login page if logged in', () => {
    cy.login(adminUsername, adminPassword);
    cy.visit('/login');
    cy.location('pathname').should('equal', '/');
  });

  [
    {
      description: 'should not submit with blank username',
      username: '',
      password: faker.string.alphanumeric(),
    },
    {
      description: 'should not submit with blank password',
      username: faker.string.alphanumeric(),
      password: '',
    },
  ].forEach(({ description, username, password }) => {
    it(description, () => {
      cy.login(username, password);
      cy.location('pathname').should('equal', '/login');
      cy.getBySel('login-submit').should('be.disabled');
    });
  });

  it('should error after invalid credentials', () => {
    const username = '!';
    const password = '123';

    cy.login(username, password);
    cy.location('pathname').should('equal', '/login');
    cy.getBySel('toast').should('be.visible').and('contain.text', 'Bad credentials');
  });
});

describe('Register', function () {
  it('should redirect to home page after register', () => {
    const username = faker.string.alphanumeric({ length: { min: 3, max: 20 } });
    const email = faker.internet.email();
    const password = faker.string.alphanumeric(8);

    cy.register(username, email, password);
    cy.location('pathname').should('equal', '/');
    cy.verifyLogin().should('be.true');
  });

  it('should redirect off of register page if registered', () => {
    const username = faker.string.alphanumeric({ length: { min: 3, max: 20 } });
    const email = faker.internet.email();
    const password = faker.string.alphanumeric(8);

    cy.register(username, email, password);
    cy.visit('/register');
    cy.location('pathname').should('equal', '/');
  });

  [
    {
      description: 'should not submit with blank username',
      username: '',
      email: faker.internet.email(),
      password: faker.string.alphanumeric(8),
    },
    {
      description: 'should not submit with blank email',
      username: faker.string.alphanumeric({ length: { min: 3, max: 20 } }),
      email: '',
      password: faker.string.alphanumeric(8),
    },
    {
      description: 'should not submit with blank password',
      username: faker.string.alphanumeric({ length: { min: 3, max: 20 } }),
      email: faker.internet.email(),
      password: '',
    },
    {
      description: 'should not submit with invalid username',
      username: 'invalidusername!',
      email: faker.internet.email(),
      password: faker.string.alphanumeric(8),
    },
    {
      description: 'should not submit with invalid email',
      username: faker.string.alphanumeric({ length: { min: 3, max: 20 } }),
      email: 'invalidemail!',
      password: faker.string.alphanumeric(8),
    },
    {
      description: 'should not submit with invalid password',
      username: faker.string.alphanumeric({ length: { min: 3, max: 20 } }),
      email: faker.internet.email(),
      password: '12345',
    },
    {
      description: 'should not submit with invalid confirm password',
      username: faker.string.alphanumeric({ length: { min: 3, max: 20 } }),
      email: faker.internet.email(),
      password: '12345678',
      confirmPassword: '1234567890',
    },
  ].forEach(({ description, username, email, password, confirmPassword }) => {
    it(description, () => {
      cy.register(username, email, password, confirmPassword);
      cy.location('pathname').should('equal', '/register');
      cy.getBySel('register-submit').should('be.disabled');
    });
  });

  [
    {
      description: 'should error with duplicate username',
      username: adminUsername,
      email: faker.internet.email(),
      password: faker.string.alphanumeric(8),
      expect: `User '${adminUsername}' already exists.`,
    },
    {
      description: 'should error with duplicate email',
      username: faker.string.alphanumeric({ length: { min: 3, max: 20 } }),
      email: adminEmail,
      password: faker.string.alphanumeric(8),
      expect: `Email '${adminEmail}' already in use.`,
    },
  ].forEach(({ description, username, email, password, expect }) => {
    it(description, () => {
      cy.register(username, email, password);
      cy.location('pathname').should('equal', '/register');
      cy.getBySel('toast').should('be.visible').and('contain.text', expect);
    });
  });
});

describe('Session', function () {
  it('should be logged in after refresh', () => {
    cy.login(adminUsername, adminPassword);
    cy.reload();
    cy.verifyLogin().should('be.true');
  });

  it('should be logged out after logout', () => {
    cy.login(adminUsername, adminPassword);
    cy.verifyLogin().should('be.true');

    // Logout
    cy.visit('/');
    cy.getBySel('sidebar-logout-button').click();

    cy.verifyLogin().should('be.false');
  });

  it('should be logged in after access token expires and refresh token valid', () => {
    cy.login(adminUsername, adminPassword);
    cy.verifyLogin().should('be.true');

    // Expire access token
    cy.window().then((win) => {
      win.localStorage.removeItem('accessToken');
    });
    cy.intercept('GET', '/api/authenticate/refresh').as('refresh');

    cy.reload();
    cy.wait('@refresh');
    cy.verifyLogin().should('be.true');
  });

  it('should be logged in after access token valid and refresh token expires', () => {
    cy.login(adminUsername, adminPassword);
    cy.verifyLogin().should('be.true');

    cy.intercept('GET', '/api/authenticate/refresh', {
      statusCode: 401,
    }).as('refresh');

    cy.reload();
    cy.get('@refresh.all').should('have.length', 0);
    cy.verifyLogin().should('be.true');
  });

  it('should be logged out after access token expires and refresh token expires', () => {
    cy.login(adminUsername, adminPassword);
    cy.verifyLogin().should('be.true');

    // Expire access and refresh token
    cy.window().then((win) => {
      win.localStorage.removeItem('accessToken');
    });
    cy.intercept('GET', '/api/authenticate/refresh', {
      statusCode: 401,
    }).as('refresh');

    cy.reload();
    cy.wait('@refresh');
    cy.verifyLogin().should('be.false');
  });
});
