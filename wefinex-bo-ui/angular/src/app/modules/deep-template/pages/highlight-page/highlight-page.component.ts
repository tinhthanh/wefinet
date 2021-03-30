import { Component, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';
import { ZaloBotService, ZaloEntity } from '../../services/zalo-bot.service';

@Component({
  selector: 'app-highlight-page',
  templateUrl: './highlight-page.component.html',
  styleUrls: ['./highlight-page.component.scss']
})
export class HighlightPageComponent implements OnInit {
  zaloEntity: ZaloEntity;
  constructor(private zaloBotService: ZaloBotService) { 
  
  }

  ngOnInit(): void {
    this.zaloBotService.get("QRCODE").subscribe( z => {
      console.log(z);
      this.zaloEntity  = z;
    });
  }
  relogin() {
    this.zaloBotService.get("command").pipe(first()).subscribe( z => {
      z.lastUpdate = new Date();
      this.zaloBotService.addUser(z, z.id).then( z => {
        console.log(z);
      });
    });
  }
}
