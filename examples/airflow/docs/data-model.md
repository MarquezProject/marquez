## Data Model

### Table `customers`

| **Column**       | **Type**    | **Description**                                                            |
|------------------|-------------|----------------------------------------------------------------------------|
| **`id`**         | `INTEGER`   | The unique ID of the customer.                                             |
| **`created_at`** | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the customer was created. |
| **`updated_at`** | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the customer was updated. |
| **`name`**       | `VARCHAR`   | The name of the customer.                                                  |
| **`email`**      | `VARCHAR`   | The email address of the customer.                                         |
| **`address`**    | `VARCHAR`   | The address of the customer.                                               |
| **`phone`**      | `VARCHAR`   | The phone number of the customer.                                          |
| **`city_id`**    | `INTEGER `  | The ID of the city related to the customer.                                |

### Table `restaurants`

| **Column**              | **Type**    | **Description**                                                              |
|-------------------------|-------------|------------------------------------------------------------------------------|
| **`id`**                | `INTEGER`   | The unique ID of the restaurant.                                             |
| **`created_at`**        | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the restaurant was created. |
| **`updated_at`**        | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the restaurant was updated. |
| **`name`**              | `VARCHAR`   | The name of the restaurant.                                                  |
| **`email`**             | `VARCHAR`   | The email address of the restaurant.                                         |
| **`address`**           | `VARCHAR`   | The address of the restaurant.                                               |
| **`phone`**             | `VARCHAR`   | The phone number of the restaurant.                                          |
| **`city_id`**           | `INTEGER`   | The ID of the city related to the restaurant.                                |
| **`business_hours_id`** | `INTEGER`   | The ID of the business hours related to the restaurant.                      |
| **`description`**       | `TEXT`      | The description of the restaurant.                                           |

### Table `business_hours`

| **Column**        | **Type**    | **Description** |
|-------------------|-------------|-----------------|
| **`id`**          | `INTEGER`   |                 |
| **`day_of_week`** | `VARCHAR`   |                 |
| **`opens_at`**    | `TIMESTAMP` |                 |
| **`closes_at `**  | `TIMESTAMP` |                 |

### Table `cities`

| **Column**       | **Type**    | **Description**                                     |
|------------------|-------------|-----------------------------------------------------|
| **`id`**         | `INTEGER`   | The unique ID of the city.                          |
| **`name`**       | `VARCHAR`   | The name of the city.                               |
| **`state`**      | `VARCHAR`   | The state, county, province, or region of the city. |
| **`zip_code`**   | `VARCHAR`   | The zip code of the city.                           |

### Table `menus`

| **Column**          | **Type**  | **Description**                               |
|---------------------|-----------|-----------------------------------------------|
| **`id`**            | `INTEGER` | The unique ID of the menu.                    |
| **`name`**          | `VARCHAR` | The name of the menu.                         |
| **`restaurant_id`** | `INTEGER` | The ID of the restaurant related to the menu. |
| **`description`**   | `TEXT`    | The description of the menu.                  |

### Table `categories`

| **Column**        | **Type**  | **Description**                             |
|-------------------|-----------|---------------------------------------------|
| **`id`**          | `INTEGER` | The unique ID of the category.              |
| **`name`**        | `VARCHAR` | The name of the category.                   |
| **`menu_id`**     | `INTEGER` | The ID of the menu related to the category. |
| **`description`** | `TEXT`    | The description of the category.            |

### Table `menu_items`

| **Column**    | **Type**  | **Description**                                      |
|-------------------|-----------|--------------------------------------------------|
| **`id`**          | `INTEGER` | The unique ID of the menu item.                  |
| **`name`**        | `VARCHAR` | The name of the menu item.                       |
| **`price`**       | `VARCHAR` | The price of the menu item.                      |
| **`category_id`** | `INTEGER` | The ID of the category related to the menu item. |
| **`description`** | `TEXT`    | The description of the menu item.                |

### Table `orders`

| **Column**         | **Type**    | **Description**                                                        |
|--------------------|-------------|------------------------------------------------------------------------|
| **`id`**           | `INTEGER`   | The unique ID of the order.                                            |
| **`placed_on`**    | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order was placed. |
| **`menu_item_id`** | `INTEGER`   | The ID of the menu item related to the order.                          |
| **`quantity`**     | `INTEGER`   | The number of the item in the order.                                   |
| **`discount_id`**  | `INTEGER`   | The ID of the discount applied to the order.                           |
| **`comment`**      | `TEXT`      | The comment of the order.                                              |

### Table `order_status`

| **Column**            | **Type**    | **Description**                                                                     |
|-----------------------|-------------|-------------------------------------------------------------------------------------|
| **`id`**              | `INTEGER`   | The unique ID of the order status.                                                  |
| **`transitioned_at`** | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order status was transitioned. |
| **`status`**          | `VARCHAR`   | The status of the order.                                                            |
| **`order_id`**        | `INTEGER`   | The ID of the order related to the status order.                                    |
| **`customer_id`**     | `INTEGER`   | The ID of the customer related to the order status.                                 |
| **`restaurant_id`**   | `INTEGER`   | The ID of the restaurant related to the order status.                               |
| **`driver_id`**       | `INTEGER`   | The ID of the driver related to the order status.                                   |

### Table `drivers`

| **Column**              | **Type**    | **Description**                                                          |
|-------------------------|-------------|--------------------------------------------------------------------------|
| **`id`**                | `INTEGER`   | The unique ID of the driver.                                             |
| **`created_at`**        | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the driver was created. |
| **`updated_at`**        | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the driver was updated. |
| **`name`**              | `VARCHAR`   | The name of the driver.                                                  |
| **`email`**             | `VARCHAR`   | The email address of the customer.                                       |
| **`phone`**             | `VARCHAR`   | The phone number of the driver.                                          |
| **`car_make`**          | `VARCHAR`   | The make of the car.                                                     |
| **`car_model`**         | `VARCHAR`   | The model of the car.                                                    |
| **`car_year`**          | `VARCHAR`   | The year of the car.                                                     |
| **`car_color`**         | `VARCHAR`   | The color of the car.                                                    |
| **`car_license_plate`** | `VARCHAR`   | The license plate number of the car.                                     |

### Table `orders_7_days`

| **Column**          | **Type**    | **Description**                                                        |
|---------------------|-------------|------------------------------------------------------------------------|
| **`order_id`**.     | `INTEGER`   | The ID of the order.                                                   |
| **`placed_on`**     | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order was placed. |
| **`discount_id`**   | `INTEGER`   | The ID of the discount applied to the order.                           |
| **`menu_id`**       | `INTEGER`   | The ID of the menu related to the order.                               |
| **`restaurant_id`** | `INTEGER`   | The ID of the restaurant related to the order.                         |
| **`menu_item_id`**  | `INTEGER`   | The ID of the menu item related to the order.                          |
| **`category_id`**   | `INTEGER`   | The ID of category related to the order.                               |

### Table `delivery_7_days`

| **Column**                | **Type**    | **Description**                                                            |
|---------------------------|-------------|----------------------------------------------------------------------------|
| **`order_id`**.           | `INTEGER`   | The ID of the order.                                                       |
| **`order_placed_on`**     | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order was placed.     |
| **`order_dispatched_on`** | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order was dispatched. |
| **`order_delivered_on`**  | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order was delivered.  |
| **`customer_email`**      | `VARCHAR`   | The email address of the customer.                                         |
| **`discount_id`**         | `INTEGER`   | The ID of the discount applied to the order.                               |
| **`menu_id`**             | `INTEGER`   | The ID of the menu related to the order.                                   |
| **`restaurant_id`**       | `INTEGER`   | The ID of the restaurant related to the order.                             |
| **`menu_item_id`**        | `INTEGER`   | The ID of the menu item related to the order.                              |
| **`category_id`**         | `INTEGER`   | The ID of category related to the order.                                   |
| **`driver_id`**           | `INTEGER`   | The ID of driver related to the order.                                     |

### Table `top_delivery_times`

| **Column**                | **Type**    | **Description**                                                            |
|---------------------------|-------------|----------------------------------------------------------------------------|
| **`order_id`**.           | `INTEGER`   | The ID of the order.                                                       |
| **`order_placed_on`**     | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order was placed.     |
| **`order_dispatched_on`** | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order was dispatched. |
| **`order_delivered_on`**  | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order was delivered.  |
| **`order_delivery_time`** | `TIMESTAMP` | An ISO-8601 timestamp representing the total time of delivery.             |
| **`customer_email`**      | `VARCHAR`   | The email address of the customer.                                         |
| **`restaurant_id`**       | `INTEGER`   | The ID of the restaurant related to the order.                             |
| **`driver_id`**           | `INTEGER`   | The ID of driver related to the order.                                     |


### Table `discounts`

| **Column**           | **Type**    | **Description**                                                       |
|----------------------|-------------|-----------------------------------------------------------------------|
| **`id`**             | `INTEGER`   | The unique ID of the discount.                                        |
| **`amount_off`**     | `INTEGER`   | The amount of the discount.                                           |
| **`customer_email`** | `VARCHAR`   | The email address of the customer.                                    |
| **`starts_on`**      | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the discount starts. |
| **`ends_on`**        | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the discounts ends.  |


### Table `popular_orders_day_of_week`

| **Column**                | **Type**    | **Description**                                                        |
|---------------------------|-------------|------------------------------------------------------------------------|
| **`order_day_of_week`**   | `VARCHAR`   | The day of week of the order.                                          |
| **`order_placed_on`**     | `TIMESTAMP` | An ISO-8601 timestamp representing the date/time the order was placed. |
| **`orders_placed`**       | `INTEGER`   | The number of orders placed on day of week.                            |

