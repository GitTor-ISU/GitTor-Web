import { ContentComponent } from '@shared/components/z-layout/content.component';
import { FooterComponent } from '@shared/components/z-layout/footer.component';
import { HeaderComponent } from '@shared/components/z-layout/header.component';
import { LayoutComponent } from '@shared/components/z-layout/layout.component';
import {
  SidebarComponent,
  SidebarGroupComponent,
  SidebarGroupLabelComponent,
} from '@shared/components/z-layout/sidebar.component';

export const LayoutImports = [
  LayoutComponent,
  HeaderComponent,
  FooterComponent,
  ContentComponent,
  SidebarComponent,
  SidebarGroupComponent,
  SidebarGroupLabelComponent,
] as const;
