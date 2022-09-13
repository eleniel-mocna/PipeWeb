# NGS Pipe web

This is a try for a web frontend for the
[Nextgen Pipe Shell project](https://github.com/eleniel-mocna/nextgen_pipe).
For more information read the README that is there. There is a tl;dr in the next section.

### Nextgen Pipe

The Nextgen Pipe provides a shell interface for usage of bioinformatics tools for
NGS (new generation sequencing) pipelines. The pipeline is then a bash script
which has the following arguments:

- A config file with not often changing configuration
- N-ths of arguments for N samples (arguments are mostly filenames).

E.g. Let's say we've got a script `say_hi_to_two_people.sh`.
To use it we can run the following command:

```shell
say_hi_to_two_people.sh /configs/config_file.sh resultFile Ester Peter
```

Where `/configs/config_file.sh` might look like this:

```shell
say_hi_to_two_people_HELLO="Hello and welcome, "
```

And get contents of `result_file` of:

```
Hello and welcome, Ester, Peter.
```

## Run

To run this program, it is recommended to run it from docker
because the initial setup will be done automatically. But it still should
work on any linux machine.

### Docker

Use the IntelliJ Docker build
or run the following commands from the terminal.

```(shell)
$ docker build -t pipe_web .
$ docker run -p <empty_port>:8080 pipe_web
```

### Maven

```
$ mvn spring-boot:run
```

## Functionality

### Login

After starting the app you can access only the login page.
Here you can log in to an existing account or add a user
(because this app should be run only in
secure networks with only trusted people having
access, this shouldn't be a problem).
![img.png](readme_images/img.png)
For the defaultUser the password is `1234`.

### Adding a user

At the login page you can also add a user by clicking
the `Add user` button.
![img_1.png](readme_images/img_1.png)
Here you can enter the user's name, password and path
to his root folder.

### User folder structure

Each user has his own folder, where he can store
all needed data. Also there is a `scripts` folder
in which all of his scripts are saved.

### Adding your first script

After logging in you'll see a file explorer in your
root folder. And in the right upper corner you can
add a script:
![img_2.png](readme_images/img_2.png)
Here you will see a file manager in which
you can navigate to the script you want to add
and press X to fill in the form on the left.
![img_3.png](readme_images/img_3.png)
Now just submit the path and the script
will be added.
![img_4.png](readme_images/img_4.png)

## Running the script

Now you can go back to the home screen
(by hitting the NGS button in the
left upper corner).
Here choose the script you want to run:
![img_5.png](readme_images/img_5.png)

Then you will get the arguments menu. Here you can specify the
config file and the arguments. You can fill these by hand
or by using the `x` buttons in the file manager.
Also hints for files in your root directory will
be provided by the browser.

You can leave the config file empty and the
default config file will be used.

After all arguments are filled in run the script by submitting 
the form by the button below the arguments.

![img_6.png](readme_images/img_6.png)

## TODO (ALPHA 1.0)

- Apply predictions for K layers of the FileTree
- Add auto-refreshes to RUNs