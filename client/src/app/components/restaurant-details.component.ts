import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { RestaurantService } from '../restaurant-service';
import { Restaurant, Comment } from '../models';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SharingService } from '../sharing.service';

@Component({
  selector: 'app-restaurant-details',
  templateUrl: './restaurant-details.component.html',
  styleUrls: ['./restaurant-details.component.css']
})
export class RestaurantDetailsComponent implements OnInit {

  param$!: Subscription
  restaurant!: string
  restInfo!: Restaurant
  mapImage!: string
  form!: FormGroup

  cuisine!: string
	
	// TODO Task 4 and Task 5
	// For View 3
  constructor(private ar: ActivatedRoute, private svc: RestaurantService, private fb: FormBuilder, 
              private router: Router, private sharingSvc: SharingService) {}

  ngOnInit(): void {
    this.form = this.createForm()
    this.param$ = this.ar.params.subscribe(
      (param) => {
        this.restaurant = param['rName']
        this.cuisine = this.sharingSvc.sharingValue
        console.log(this.cuisine)

        this.svc.getRestaurant(this.restaurant)
          .then(result => {
            console.log(result)
            this.restInfo = result
          }).catch(error => {
            console.log(error)
          })
      }
    )
  }

  checkFormInvalid() {
    return this.form.invalid
  }

  processForm() {
    console.log("processing form")
    const comment = this.form.value as Comment
    comment.restaurantId = this.restInfo.restaurantId
    this.svc.postComment(comment)
        .then(result => {
          console.log(result)
        }).catch(error => {
          console.log(error)
        })

    this.router.navigate(['/'])
  }

  private createForm(): FormGroup {
    return this.fb.group({
      name: this.fb.control<string>('', [ Validators.required, Validators.minLength(3) ]),
      rating: this.fb.control<number>(1, [ Validators.min(1), Validators.max(5) ]),
      text: this.fb.control<string>('', [ Validators.required ])
    })
  }

}
