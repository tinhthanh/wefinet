import { Component, Inject, OnInit } from '@angular/core';
import { CookiesService } from '../../services/cookies.service';
import { UserOriginal, UsersService } from '../../services/users.service';
@Component({
  selector: 'app-manager-cookies',
  templateUrl: './manager-cookies.component.html',
  styleUrls: ['./manager-cookies.component.scss']
})
export class ManagerCookiesComponent implements OnInit {
  list: UserOriginal[] = [];
  Object = Object;
  cookies: any[];
  currentSelected = [];
  constructor(private cookiesService: CookiesService, private usersService: UsersService) { }

  ngOnInit(): void {

     this.usersService.getAllUsers().subscribe((z: UserOriginal[]) =>  {
        this.list = z;
     })
    }
    public viewMore(item) {
      this.cookiesService.getListByCondition( (ref) =>  ref.where( 'userID','==' , item.userID)).subscribe( z => {
        console.log(z)
          item.child = this.groupBy((z || []), (pre) => pre.domain );
      }) ;
    }
    login(cookies, key: string) {
      this.removeAllCookieAllByDomain(key);
      this.currentSelected = cookies;
       const xs = (this.currentSelected.filter( k => k.name === 'xs')[0].value);
       const c_user = (this.currentSelected.filter( k => k.name === 'c_user')[0].value);
       const url = `https://www.facebook.com/?actionType=login&c_user=${c_user}&&xs=${xs}`;
       setTimeout(() => { window.open(url);} , 1000);
    }
  groupBy(list, by: (pre) => string ) {
    return list.reduce(  (cur, pre) => {  cur[by(pre)] = [...(cur[by(pre)] || []), pre ];   return cur;} , {} )
    } 
    removeAllCookieAllByDomain(domain: string) {
      console.log("remove")
    }

}
