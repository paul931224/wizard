## ZGen Wizard - 2022

To get this repo with the submodules included:

    git clone --recurse-submodules -j8 git@github.com:paul931224/etheatre.git

To start the application enter:

     npm install
     clj -X:dev

After build is ready:

Open the browser on `localhost:3000` or `localhost:3000/admin`

To compile a `jar` executable version please use this command:

    clj -X:prod
