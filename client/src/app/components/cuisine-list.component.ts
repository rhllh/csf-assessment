import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RestaurantService } from '../restaurant-service';

@Component({
  selector: 'app-cuisine-list',
  templateUrl: './cuisine-list.component.html',
  styleUrls: ['./cuisine-list.component.css']
})
export class CuisineListComponent implements OnInit {

	// TODO Task 2
	// For View 1
  cuisines: string[] = []

  constructor(private router: Router, private svc: RestaurantService) {}

  ngOnInit(): void {
    // display list of cuisines from backend
    this.svc.getCuisineList()
      .then(result => {
        this.cuisines = result['cuisines']
      }).catch(error => {
        console.log(error)
      })
  }

}
