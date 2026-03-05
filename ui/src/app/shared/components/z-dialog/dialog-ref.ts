import type { OverlayRef } from '@angular/cdk/overlay';
import { isPlatformBrowser } from '@angular/common';
import { EventEmitter, Inject, PLATFORM_ID } from '@angular/core';

import { filter, fromEvent, Subject, takeUntil } from 'rxjs';

import type { ZardDialogComponent, ZardDialogOptions } from './dialog.component';

const enum eTriggerAction {
  CANCEL = 'cancel',
  OK = 'ok',
}

export class ZardDialogRef<T = any, R = any, U = any> {
  private destroy$ = new Subject<void>();
  private isClosing = false;
  protected result?: R;
  componentInstance: T | null = null;

  constructor(
    private overlayRef: OverlayRef,
    private config: ZardDialogOptions<T, U>,
    private containerInstance: ZardDialogComponent<T, U>,
    @Inject(PLATFORM_ID) private platformId: object,
  ) {
    this.containerInstance.cancelTriggered.subscribe(() => void this.trigger(eTriggerAction.CANCEL));
    this.containerInstance.okTriggered.subscribe(() => void this.trigger(eTriggerAction.OK));

    if ((this.config.zMaskClosable ?? true) && isPlatformBrowser(this.platformId)) {
      this.overlayRef
        .outsidePointerEvents()
        .pipe(takeUntil(this.destroy$))
        .subscribe(() => void this.trigger(eTriggerAction.CANCEL));
    }

    if (isPlatformBrowser(this.platformId)) {
      fromEvent<KeyboardEvent>(document, 'keydown')
        .pipe(
          filter(event => event.key === 'Escape'),
          takeUntil(this.destroy$),
        )
        .subscribe(() => void this.trigger(eTriggerAction.CANCEL));
    }
  }

  close(result?: R) {
    if (this.isClosing) {
      return;
    }

    this.isClosing = true;
    this.result = result;

    if (isPlatformBrowser(this.platformId)) {
      const hostElement = this.containerInstance.getNativeElement();
      hostElement.classList.add('dialog-leave');
    }

    setTimeout(() => {
      if (this.overlayRef) {
        if (this.overlayRef.hasAttached()) {
          this.overlayRef.detachBackdrop();
        }
        this.overlayRef.dispose();
      }

      if (!this.destroy$.closed) {
        this.destroy$.next();
        this.destroy$.complete();
      }
    }, 150);
  }

  private async trigger(action: eTriggerAction): Promise<void> {
    const trigger = { ok: this.config.zOnOk, cancel: this.config.zOnCancel }[action];

    if (trigger instanceof EventEmitter) {
      trigger.emit(this.getContentComponent());
    } else if (typeof trigger === 'function') {
      try {
        const result = await Promise.resolve(trigger(this.getContentComponent()));
        this.closeWithResult(result);
      } catch {
        // Keep dialog open when callback fails.
      }
    } else {
      this.close();
    }
  }

  private getContentComponent(): T {
    return this.componentInstance as T;
  }

  private closeWithResult(result: unknown): void {
    if (result !== false) {
      this.close(result as R);
    }
  }
}
