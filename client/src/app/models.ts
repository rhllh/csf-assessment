// Do not change these interfaces
export interface Restaurant {
	restaurantId: string
	name: string
	cuisine: string
	address: string
	coordinates: number[]
	image_url: string
}

export interface Comment {
	name: string
	rating: number
	restaurantId: string
	text: string
}
