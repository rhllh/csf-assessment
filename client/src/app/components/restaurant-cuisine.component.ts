import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { RestaurantService } from '../restaurant-service';
import { SharingService } from '../sharing.service';

@Component({
  selector: 'app-restaurant-cuisine',
  templateUrl: './restaurant-cuisine.component.html',
  styleUrls: ['./restaurant-cuisine.component.css']
})
export class RestaurantCuisineComponent implements OnInit {

  param$!: Subscription
  cuisine!: string
  restaurants: string[] = []
	
	// TODO Task 3
	// For View 2
  constructor(private ar: ActivatedRoute, private router: Router, private svc: RestaurantService,
              private sharingSvc: SharingService) {}

  ngOnInit(): void {
    this.param$ = this.ar.params.subscribe(
      (param) => { 
        this.cuisine = param['name']
        this.sharingSvc.sharingValue = this.cuisine
        console.log(this.sharingSvc.sharingValue)
        
        this.svc.getRestaurantsByCuisine(this.cuisine)
            .then(result => {
              this.restaurants = result['restaurants']
            }).catch(error => {
              console.log(error)
            })
      }
    )
  }
}
