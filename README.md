# PasteUploader
PasteUploader is a lightweight and easy to use file sharing open source software written in Java, that's main goal is to serve a quick file upload via remote file sharing APIs. 

![Demo screenshot image](https://i.imgur.com/csPyJWY.png)

## Clipboard uploads
It is mainly designed to upload clipboard contents like snippets of images, without having a need of 3rd party screenshot capture and sharing software. For example, in Microsoft Windows you can use the built in "Snipping tool", that captures screen and stores the image automatically to the clipboard. When launching PasteUploader, the image is automatically detected and prepared to be uploaded without a need to select it from the file system. If you need to adjust the image, you can simply capture a new one and paste it in the GUI with a "`Ctrl + V`" keyboard combo.
If the clipboard contents are "text like", then the content string is uploaded as a text.

## Other file types
PasteUploader can also handle any other file format uploads, but then the files can't be automatically loaded from the clipboard and have to be specified, by using a file picker, or the file can be directly dragged into the GUI.

## Automatic upload response parsing
To make the workflow of the PasteUploader as quick as possible, it is possible to set a JSON key name that's value is going to be extracted from a successful server response and copied to the clipboard. It is also possible to automatically exit the program after the expected JSON value is copied.  
This means that the usage of the program can be just as quick as:  
1) Opening the app (Media to upload is already in the clipboard)
2) Pressing "Enter" keyboard key or clicking GUI "Upload!" button.
3) The sharing URL is in user's clipboard and the PasteUploader terminates itself.

## Current limitations
Currently the following usage limitations for this program include:
* Only `POST` requests are supported
* Only `multipart/form-data` encoding is supported for the requests
* Only `application/json` responses can be automatically parsed and can only extract JSON value from the root object, no complex or *regex like* extractions
* No upload file previews for files picked with the file picker or dragged files into the window
* Requires non headless environment (ability to create a GUI)
* One file upload at a time is supported
* Not possible to define request header/cookies or gather them from the response

## Configuration
PasteUploader takes program flag arguments to manage the configuration. You can create a simple batch files or shell scripts to store multiple upload profiles.

| **Option**                        | **Description**                                                                                                                                                                                             | **Is required?** |
|-----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|
| `-c,--COPY_JSON_FIELD_NAME <arg>` | This option enables automatically copying a string value from the resulting successful JSON response. For example, sharing link for the uploaded file. This does not support accessing nested JSON objects. | ❌                |
| `-e,--EXTRA_FORM_DATA <arg>`      | Extra data for the `multipart/form-data`. Format: `key=value`. Unlimited args.                                                                                                                              | ❌                |
| `-f,--FORM_FILE_KEY_NAME <arg>`   | Name of the key in `multipart/form-data` for the media bytes that will be sent.                                                                                                                             | ✅                |
| `-n,--CUSTOM_FILE_NAME <arg>`     | Provides a custom file name for all uploads. The name can be any string and it does not have to match the name of the file you want to upload.                                                              | ❌                |
| `-q,--AUTO_QUIT`                  | Enables automatically exiting the program after successfully copying the desired JSON field with the `-c,--COPY_JSON_FIELD_NAME <arg>` option.  This is option is ignored if the `-c` value is not present. | ❌                |
| `-u,--URL <arg>`                  | Full URL of the upload service where the media will be uploaded to.                                                                                                                                         | ✅                |


## License
This project is under a [Mozilla Public License Version 2.0](./LICENSE) license.