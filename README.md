# NGS Pipe web
## Run
### Docker
```(shell)
$ docker build -t pipe_web .
$ docker run -p <empty_port>:8080 pipe_web
```
### Maven
```
$ mvn spring-boot:run
```
## TODO (ALPHA 1.0)

[//]: # (- Add Refresh for FileTreeView)
- Apply predictions for K layers of the FileTree
[//]: # (- Put the FileTreeView into a container providing scrolling etc.)
[//]: # (- Sort out a table of FileTreeView &#40;try hard setting a width for each column and do tables in a table...&#41;)
[//]: # (- Repair sort and filter)
[//]: # (- Make sure that the output function works out of FTV)
- Add auto-refreshes to RUNs