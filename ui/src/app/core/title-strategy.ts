import { inject, Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { TitleStrategy, RouterStateSnapshot } from '@angular/router';

@Injectable()
export class AppTitleStrategy extends TitleStrategy {
    private readonly title = inject(Title);

    updateTitle(snapshot: RouterStateSnapshot): void {
        // PageTitle is equal to the "Title" of a route if it's set
        // If its not set it will use the "title" given in index.html
        const pageTitle = this.buildTitle(snapshot);
        this.title.setTitle(pageTitle ? `${pageTitle} | GitTor` : 'GitTor');
    }
}
