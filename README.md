# SlackShot

A Java Spring Boot application that takes screenshots of websites and posts them to Slack automatically. This is a Java port of the original Go-based wamper project.

## Features

- **Automated Screenshots**: Takes screenshots of websites at scheduled intervals using Selenide
- **Slack Integration**: Automatically posts screenshots to Slack channels
- **Login Support**: Supports Jenkins, GitHub, and New Relic login forms
- **REST API**: Full REST API for managing sites and tasks
- **Scheduling**: Configurable intervals for screenshot and Slack tasks
- **Database Storage**: H2 database for storing sites, screenshots, and tasks
- **Modern Web Automation**: Uses Selenide for robust and reliable web automation

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Chrome browser (for taking screenshots)
- ChromeDriver (automatically managed by Selenide)

## Security Configuration

**⚠️ IMPORTANT: Never commit sensitive data to version control!**

### Environment Variables (Recommended)
```bash
export SLACK_SERVICE_KEY=xoxb-your-slack-bot-token
export AUTH_KEY=your-auth-key
export SCREENSHOT_KEY=your-screenshot-key
export SCREENSHOT_SERVICE_KEY=your-screenshot-service-key
```

### Local Configuration File
1. Copy the example configuration:
   ```bash
   cp config-example.yml config-local.yml
   ```
2. Edit `config-local.yml` with your actual tokens
3. The file is already in `.gitignore` and won't be committed

### Sensitive Data That Should NEVER Be Committed:
- ❌ Slack Bot Tokens (`xoxb-...`)
- ❌ Authentication Keys
- ❌ Passwords
- ❌ API Keys
- ❌ Database credentials (in production)

## Configuration

The application uses environment variables for sensitive configuration. Create a `config-local.yml` file or set environment variables:

```properties
# Required configuration (use environment variables)
SLACK_SERVICE_KEY=xoxb-your-slack-bot-token
AUTH_KEY=your-auth-key
SCREENSHOT_KEY=your-screenshot-key

# Optional configuration
webserver.port=3030
log.dir=log
debug.port=6060
debug.user=user
debug.pass=pass
eventstore.host=localhost
```

## Selenide Configuration

The application uses Selenide for web automation with the following default configuration:

```yaml
selenide:
  browser: chrome
  headless: true
  timeout: 10000
  browserSize: 1920x1200
  screenshots: true
  savePageSource: false
```

## Running the Application

1. **Clone and build**:
   ```bash
   git clone <repository-url>
   cd SlackShot
   mvn clean install
   ```

2. **Set up your configuration**:
   ```bash
   # Option 1: Environment variables
   export SLACK_SERVICE_KEY=xoxb-your-token
   export AUTH_KEY=your-auth-key
   
   # Option 2: Local config file
   cp config-example.yml config-local.yml
   # Edit config-local.yml with your tokens
   ```

3. **Run the application**:
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**:
   - Application: http://localhost:3030
   - H2 Console: http://localhost:3030/h2-console
   - API Documentation: Available at `/api/**` endpoints

## API Endpoints

### Sites Management

- `PUT /api/site` - Add a new site
- `GET /api/site?name={siteName}` - Get latest screenshot for a site
- `POST /api/site/{name}/screenshot` - Take a new screenshot immediately
- `GET /api/site/list` - Get all sites
- `GET /api/site/{name}` - Get site by name
- `DELETE /api/site/{name}` - Delete a site

### Screenshot Tasks

- `PUT /api/screenshot/task` - Add a new screenshot task
- `GET /api/screenshot/tasks` - Get all screenshot tasks
- `DELETE /api/screenshot/task/{id}` - Delete a screenshot task

### Slack Tasks

- `PUT /api/slack/task` - Add a new Slack task
- `GET /api/slack/tasks` - Get all Slack tasks
- `DELETE /api/slack/task/{id}` - Delete a Slack task

## Example Usage

### Adding a Site

```bash
curl -X PUT http://localhost:3030/api/site \
  -H "Authorization: your-auth-key" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "example-site",
    "url": "https://example.com",
    "loginType": "NONE"
  }'
```

### Adding New Relic Dashboard

```bash
curl -X PUT http://localhost:3030/api/site \
  -H "Authorization: your-auth-key" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "newrelic-cost-manager",
    "url": "https://one.newrelic.com/dashboards/detail/MTQxMjg3M3xWSVp8REFTSEJPQVJEfGRhOjQ0MzcwNDk?duration=86400000&state=d0fbd92c-84ed-0ee6-7512-3526b1527949",
    "loginType": "NEWRELIC",
    "username": "ext_jamadeo@dlocal.com"
  }'
```

### Taking a Screenshot Immediately

```bash
curl -X POST http://localhost:3030/api/site/example-site/screenshot \
  -H "Authorization: your-auth-key"
```

### Adding a Screenshot Task

```bash
curl -X PUT http://localhost:3030/api/screenshot/task \
  -H "Authorization: your-auth-key" \
  -H "Content-Type: application/json" \
  -d '{
    "siteName": "example-site",
    "time": "2024-01-01T10:00:00",
    "interval": "PT1H"
  }'
```

### Adding a Slack Task

```bash
curl -X PUT http://localhost:3030/api/slack/task \
  -H "Authorization: your-auth-key" \
  -H "Content-Type: application/json" \
  -d '{
    "siteName": "example-site",
    "time": "2024-01-01T10:00:00",
    "interval": "PT1H",
    "slackToken": "xoxb-your-slack-token",
    "slackChannel": "#general"
  }'
```

## Supported Login Types

### 1. No Authentication (NONE)
For public websites that don't require login.

### 2. Jenkins (JENKINS)
For Jenkins instances with username/password authentication.

### 3. GitHub (GITHUB)
For GitHub login forms.

### 4. New Relic (NEWRELIC)
For New Relic dashboards with email-based authentication and Google SSO support.

**Note**: For New Relic, you only need to provide your email address. The system will handle the Google SSO flow automatically if you're already logged into Gmail.

## Slack Bot Setup

To use the Slack integration, you need to create a Slack app with the following permissions:

### Required Scopes
- `chat:write` - Send messages to channels
- `files:write` - Upload files to channels

### Bot Manifest Example

```json
{
    "display_information": {
        "name": "SlackShot",
        "description": "A tool for posting website screenshots to Slack",
        "background_color": "#2c2d30"
    },
    "features": {
        "bot_user": {
            "display_name": "SlackShot",
            "always_online": false
        }
    },
    "oauth_config": {
        "scopes": {
            "bot": [
                "chat:write",
                "files:write"
            ]
        }
    },
    "settings": {
        "org_deploy_enabled": false,
        "socket_mode_enabled": false,
        "token_rotation_enabled": false
    }
}
```

## Architecture

The application follows a typical Spring Boot architecture:

- **Controllers**: REST API endpoints
- **Services**: Business logic for screenshots and Slack integration
- **Repositories**: Data access layer using Spring Data JPA
- **Models**: JPA entities for database storage
- **Configuration**: Security and Selenide configuration

### Key Components

1. **ScreenshotService**: Handles taking screenshots using Selenide
2. **SlackService**: Manages Slack API integration
3. **Scheduled Tasks**: Automatically processes screenshot and Slack tasks
4. **Security**: API key authentication for all endpoints
5. **Selenide**: Modern web automation framework for reliable screenshots

## Why Selenide?

Selenide was chosen over Selenium for several advantages:

- **Simpler API**: More readable and concise code
- **Automatic waits**: Built-in smart waiting mechanisms
- **Automatic WebDriver management**: No need to manually manage WebDriver lifecycle
- **Better error handling**: More descriptive error messages
- **Automatic screenshots**: Automatic screenshots on test failures
- **Modern approach**: Built on top of Selenium but with better abstractions

## Development

### Building

```bash
mvn clean compile
```

### Testing

```bash
mvn test
```

### Running with Docker

```bash
docker build -t slackshot .
docker run -p 3030:3030 slackshot
```

## Troubleshooting

### Common Issues

1. **ChromeDriver not found**: Selenide automatically downloads and manages ChromeDriver
2. **Screenshots not working**: Ensure Chrome browser is installed
3. **Slack integration failing**: Verify Slack token and channel permissions
4. **Database issues**: Check H2 console at http://localhost:3030/h2-console
5. **Selenide configuration**: Check `application.yml` for Selenide settings
6. **New Relic login issues**: Ensure you're logged into Gmail for SSO to work
7. **Authentication errors**: Verify your `AUTH_KEY` environment variable is set correctly

### Logs

Check application logs for detailed error information. Log level can be configured in `application.yml`.

### Selenide Reports

Selenide automatically generates reports in `target/selenide-reports/` directory when screenshots are taken.

## License

This project is licensed under the same license as the original wamper project. 