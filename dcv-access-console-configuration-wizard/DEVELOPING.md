# Developing dcv-access-console-configuration-wizard

Put your notes on developing for/contributing to this package here.

## Running
This project requires `Python >= 3.8` to be installed on the system. The easiest way to manage different python
installations is by using `mise`.

Once you are using a valid version of Python, you need to build the environment with
```
brazil-build
```
Then you can activate the virtual env with
```
source .venv/bin/activate
```
Finally, you can get help for the Wizard with
```
python3 src/wizard --help
```

Arguments can be specified either manually with flags, such as
```
python3 src/wizard --broker_address=localhost
```
or by just running the wizard and responding to the prompts.

You can use the `--quiet` flag to block prompts for optional parameters, or `--force` to block prompts for all options. 