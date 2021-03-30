import { Injectable } from '@angular/core';
import { AngularFirestore } from '@angular/fire/firestore';
import { Observable } from 'rxjs';
import { Entity, FirestoreCrudService } from './firestore-crud.service';

@Injectable()
export class ZaloBotService {


  private crudService: FirestoreCrudService<ZaloEntity>;

  // AngularFirestore should be found by Angular DI System
  constructor(private afs: AngularFirestore) {
      // Let's create our CrusService and use the a Collection with the name 'todos'
      this.crudService = new FirestoreCrudService<ZaloEntity>(afs, 'zalo_bot');
  }

  addUser(user: ZaloEntity, id?: string) {
      return this.crudService.add(user, id);
  }

  updateUser(user: ZaloEntity) {
      return this.crudService.update(user);
  }

  deleteUser(user: ZaloEntity) {
      return this.crudService.delete(user.id);
  }

  getAllUsers(): Observable<ZaloEntity[]> {
      return this.crudService.list();
  }
  get(id):Observable<ZaloEntity>{
    return this.crudService.get(id);
  }
}
export class ZaloEntity implements Entity {
    id?: string;
    qr: string;
    lastUpdate: any;
}