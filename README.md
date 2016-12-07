# GitHub Releases Downloader

A quick and dirty application that allows you to download APKs from your GitHub releases.
After the APK file downloads, an install is automatically triggered. 
This can be useful for testers and small teams to distribute builds internally. 
It also really helps if you build APKs on Travis and push the APKs to GitHub releases.

## Build the application

To build the application, you need to add 2 values in local.properties

    github_oauth_token=token <your oauth token here>
    github_releases_url=https://api.github.com/repos/<your account>/<your repo>/releases

You can also optionally specify these as environment variables

    GITHUB_OAUTH_TOKEN
    GITHUB_RELEASES_URL
    
You will ofcourse, need to [generate your own GitHub OAuth token which is simple](https://help.github.com/articles/creating-an-access-token-for-command-line-use/).

## License

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
