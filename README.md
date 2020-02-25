## Leave Application System (Sample Code) ##
To jump straight to the .java files, [*click here*](https://github.com/willymcgeejr/LeaveApplicationSample/tree/master/leaveappmaven/src/main/java/willmcg)!
---
#### This repository contains sample code for one of the faces of an application suite that allows organization employees to submit leave requests to their managers internally. The bulk of the work is done by the ApplicationController class.

#### In the repo's current state, the code is *non-functional*, as I have removed all identifying information and filepaths with the string "REDACTED" at the request of my previous employer. Below is an outline is how the application works, along with some screenshots.

Upon launch, the application detects the user's name, manager, and available hours via an Active Directory query.

In this example, I have myself set as my own manager in Active Directory for testing purposes.

<img src="https://github.com/willymcgeejr/LeaveApplicationSample/blob/master/SampleImages/leave1.JPG"  width="400" height="410" />

All employees have access to this face of the application, and can pile multiple leave requests into the same submission.

<img src="https://github.com/willymcgeejr/LeaveApplicationSample/blob/master/SampleImages/leave2.JPG"  width="400" height="410" />

<img src="https://github.com/willymcgeejr/LeaveApplicationSample/blob/master/SampleImages/leave3.JPG"  width="400" height="410" />


**!!!**

**This is the extent of the provided sample code's functionality, but the remainder of the project is outlined below if you are curious.**

**!!!**


---

Once the request is submitted, the employee will get a confirmation email...

<img src="https://github.com/willymcgeejr/LeaveApplicationSample/blob/master/SampleImages/email.JPG"  width="500" height="300" />

While their managers will get a similar email with a button at the bottom, that will take them to an approval page.

<img src="https://github.com/willymcgeejr/LeaveApplicationSample/blob/master/SampleImages/approval.png"  width="400" height="240" />

Once the request is approved or denied, the original sender will get an email that notifies them of the status of their application. If it is approved, the HR mailbox is also notified of the employee's approved absence.

---

There is also an administration side of the application, where approved users can view and modify each employee's hours. 
Here, they can also view a protected copy the changelog for all employees, sorted by date.

<img src="https://github.com/willymcgeejr/LeaveApplicationSample/blob/master/SampleImages/admin.JPG"  width="500" height="450" />
