import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { ManagerCookiesComponent } from './pages/manager-cookies/manager-cookies.component';
import { ViewCodeComponent } from './pages/view-code/view-code.component';

const routes: Routes = [
{
  path: '',
  component: ViewCodeComponent,
  children: [ 
    {
      path: '',
      component: ManagerCookiesComponent
    }
  ]
}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DeepTemplateRoutingModule { }
