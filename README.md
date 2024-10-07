# ReservationApp

## Requirement

There are two kinds of users, **providers** and **clients**. Providers have a schedule where they are available to see clients. Clients want to book time, in advance, on that schedule.

**Providers**

- Have an id.
- Have a schedule
    - On Friday the 13th of August, I want to work between 8am and 3pm.

**Clients**

- Have an id.
- Want to reserve a 15m time ‘slot’ from a providers schedule.
    - Reservations expire after 30 mins if not confirmed.
    - Reservations must be made at least 24 hours in advance.
- Want to be able to confirm a reservation.



Build the front end for a mobile web application that covers as many of the following as possible in the time allotted:

- Allows providers to submit times they’d like to work on the schedule.
- Allows clients to list available slots.
- Allows clients to reserve an available slot.
- Allows clients to confirm their reservation.

## Architecture Diagram

<img src="https://github.com/ChuliangYang/ReservationApp/blob/main/system-arch.svg" alt="Architecture Diagram" width="1200" height="1000"/>

## User Paths

<img src="https://github.com/ChuliangYang/ReservationApp/blob/main/user%20paths.png" alt="Screen" width="1200" height="500"/>

