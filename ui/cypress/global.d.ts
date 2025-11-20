declare namespace Cypress {
  interface Chainable {
    getBySel(dataTestAttribute: string, args?: any): Chainable<JQuery<HTMLElement>>;
    login(username: string, password: string): void;
    register(username: string, email: string, password: string, confirmPassword?: string): void;
    verifyLogin(): Chainable<boolean>;
  }
}
