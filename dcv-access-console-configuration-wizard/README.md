# DCV Access Console Configuration Wizard

This component is intended to create a user-friendly script for creating the four configuration files used by the
DCV Access Console, as well as creating a self-signed cert for the WebServer. The four configuration files are
- application.properties -- Used by the dcv-access-console-handler
- application.yml -- Used by the dcv-access-console-auth-server
- .env -- Used by the dcv-access-console-web-client
- nginx.conf -- Used by the dcv-access-console-web-client

## Running

This script requires Python3.x to be installed. To install the dependencies, run 
```
python3 -m pip install -r dcv_access_console_config_wizard/requirements.txt
```
and to run the script, run
```
python3 wizard.py
```
You can get help for the Wizard by running
```
python3 wizard.py --help
```
Which will show you every option and what it means. 
The script can be run either by specifying each option through a parameter, like `--handler_address=10.0.0.1` or by
just running the wizard and entering the values when prompted.

The script also has the option to pass in a JSON file of the options. If you create a JSON file with each of the options as
keys, and their associated values, you can pass the filepath (absolute or relative) to the `--input_json` option and it
will parse each specified option out of it. It will then prompt for any missing values, unless `--quiet` or `--force` 
is set. Example of the JSON file:
```json
{
  "handler_address": "10.0.0.1",
  "handler_port": 8080,
  "webclient_address": "10.0.0.1",
  "webclient_port": 3000,
  "broker_address": "192.168.0.1"
}
```