# PatlaTarLagna Matrimonial Platform - Postman Collection Guide

This document contains the API specifications and a copy-pasteable Postman Collection v2.1 JSON payload to quickly import and test the backend endpoints.

---

## 1. Setup & Environment Variables

To use this collection, set up a Postman Environment with the following variables:

| Variable Name | Default Value | Description |
| :--- | :--- | :--- |
| `baseUrl` | `http://localhost:8080` | Root URL of the Spring Boot backend |
| `jwtToken` | `{{bearer_token}}` | JWT token received on successful login. Inject automatically via authorization headers |
| `refreshToken` | `{{refresh_token}}` | Token used to fetch a new JWT token without logging in again |

---

## 2. API Endpoints Map

### Folder: 1. Authentication
*   **POST** `/api/v1/auth/register` (Register new account)
*   **POST** `/api/v1/auth/login` (Aquire JWT & Refresh tokens)
*   **POST** `/api/v1/auth/verify-email` (Verify account via mail token)
*   **POST** `/api/v1/auth/resend-verification` (Request a new validation mail)
*   **POST** `/api/v1/auth/forgot-password` (Forgot password OTP link trigger)
*   **POST** `/api/v1/auth/reset-password` (Change password with token verification)
*   **POST** `/api/v1/auth/refresh` (Request a new JWT using refresh token)
*   **POST** `/api/v1/auth/logout` (Invalidate current authentication session)

### Folder: 2. Profile Management
*   **POST** `/api/v1/profiles` (Create user profile details)
*   **PUT** `/api/v1/profiles` (Update profile details)
*   **GET** `/api/v1/profiles/me` (Retrieve self profile info)
*   **GET** `/api/v1/profiles/{userId}` (Retrieve another user's profile)
*   **POST** `/api/v1/profiles/photos` (Upload profile pictures - Multi-part)
*   **DELETE** `/api/v1/profiles/photos/{photoId}` (Delete profile picture)
*   **GET** `/api/v1/profiles/visitors` (Get users who visited your profile)

### Folder: 3. Partner Preferences
*   **GET** `/api/v1/preferences/my` (Get current search preferences)
*   **PUT** `/api/v1/preferences/my` (Update partner preferences)

### Folder: 4. Discover & Search
*   **GET** `/api/v1/search` (Advanced filters matching search)
*   **GET** `/api/v1/matches/recommendations` (Retrieve candidate profiles matching preferences)
*   **GET** `/api/v1/matches/compatibility/{targetUserId}` (Retrieve preference-compatibility percentage)

### Folder: 5. Match Interactions
*   **POST** `/api/v1/matches/interests/send/{receiverId}` (Express matrimonial interest)
*   **POST** `/api/v1/matches/interests/accept/{interestId}` (Accept request to enable chat)
*   **POST** `/api/v1/matches/interests/reject/{interestId}` (Reject request)
*   **GET** `/api/v1/matches/interests/received` (List incoming requests)
*   **GET** `/api/v1/matches/interests/sent` (List outgoing requests)
*   **GET** `/api/v1/matches/mutual` (List mutual matches)
*   **POST** `/api/v1/matches/block/{blockedId}` (Block user from seeing or messaging you)
*   **POST** `/api/v1/matches/report/{reportedId}` (Report user profile for violations)

### Folder: 6. Messaging / Chat
*   **GET** `/api/v1/chat/history/{receiverId}` (Fetch private chat log)
*   **POST** `/api/v1/chat/send` (Send message manually via HTTP)

### Folder: 7. Notifications
*   **GET** `/api/v1/notifications` (List in-app alert history)
*   **POST** `/api/v1/notifications/read/{notificationId}` (Mark notification read)

### Folder: 8. Administration (Requires ADMIN role)
*   **GET** `/api/v1/admin/stats` (Retrieve total metric dashboard logs)
*   **GET** `/api/v1/admin/verifications/pending` (List unverified member profiles)
*   **POST** `/api/v1/admin/verifications/approve/{profileId}` (Approve profile details verification)
*   **GET** `/api/v1/admin/reports` (List all reported user complaints)
*   **POST** `/api/v1/admin/reports/resolve/{reportId}` (Dismiss report or suspend user)

---

## 3. Postman Collection JSON

Copy the block below and save it as `PatlaTarLagna.postman_collection.json`, then import it directly into Postman.

```json
{
  "info": {
    "_postman_id": "402c9e83-42f7-4809-a0ae-c5cc0254ef40",
    "name": "PatlaTarLagna Matrimonial Platform API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Authentication",
      "item": [
        {
          "name": "Register User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"user@example.com\",\n  \"password\": \"Password123!\",\n  \"role\": \"NORMAL_USER\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/register",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "auth",
                "register"
              ]
            }
          }
        },
        {
          "name": "Login User",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "var jsonData = pm.response.json();",
                  "if (jsonData.data && jsonData.data.accessToken) {",
                  "    pm.environment.set(\"jwtToken\", jsonData.data.accessToken);",
                  "    pm.environment.set(\"refreshToken\", jsonData.data.refreshToken);",
                  "}"
                ],
                "type": "text/javascript"
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"email\": \"user@example.com\",\n  \"password\": \"Password123!\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/login",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "auth",
                "login"
              ]
            }
          }
        },
        {
          "name": "Verify Email",
          "request": {
            "method": "POST",
            "header": [],
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/verify-email?email=user@example.com&token=sample-token-123",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "auth",
                "verify-email"
              ],
              "query": [
                {
                  "key": "email",
                  "value": "user@example.com"
                },
                {
                  "key": "token",
                  "value": "sample-token-123"
                }
              ]
            }
          }
        },
        {
          "name": "Resend Verification",
          "request": {
            "method": "POST",
            "header": [],
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/resend-verification?email=user@example.com",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "auth",
                "resend-verification"
              ],
              "query": [
                {
                  "key": "email",
                  "value": "user@example.com"
                }
              ]
            }
          }
        },
        {
          "name": "Forgot Password Trigger",
          "request": {
            "method": "POST",
            "header": [],
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/forgot-password?email=user@example.com",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "auth",
                "forgot-password"
              ],
              "query": [
                {
                  "key": "email",
                  "value": "user@example.com"
                }
              ]
            }
          }
        },
        {
          "name": "Reset Password",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"token\": \"sample-reset-token\",\n  \"newPassword\": \"NewSecurePassword123!\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/reset-password",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "auth",
                "reset-password"
              ]
            }
          }
        },
        {
          "name": "Token Refresh",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"refreshToken\": \"{{refreshToken}}\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/refresh",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "auth",
                "refresh"
              ]
            }
          }
        },
        {
          "name": "Logout",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/auth/logout",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "auth",
                "logout"
              ]
            }
          }
        }
      ]
    },
    {
      "name": "2. Profile Management",
      "item": [
        {
          "name": "Create Profile",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Sahil Patil\",\n  \"age\": 27,\n  \"gender\": \"MALE\",\n  \"religion\": \"Hindu\",\n  \"caste\": \"Maratha\",\n  \"subCaste\": \"96 Kuli\",\n  \"motherTongue\": \"Marathi\",\n  \"height\": 175.5,\n  \"weight\": 70.0,\n  \"education\": \"B.Tech Computer Science\",\n  \"occupation\": \"Software Engineer\",\n  \"annualIncome\": 1200000.0,\n  \"maritalStatus\": \"NEVER_MARRIED\",\n  \"city\": \"Mumbai\",\n  \"state\": \"Maharashtra\",\n  \"country\": \"India\",\n  \"aboutMe\": \"Hi, I am a software professional looking for a life partner.\",\n  \"hobbies\": \"Hiking, Reading\",\n  \"lifestyle\": \"VEGETARIAN\",\n  \"familyDetails\": \"Nuclear family, well educated.\",\n  \"horoscope\": \"Not matching necessary\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/profiles",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "profiles"
              ]
            }
          }
        },
        {
          "name": "Update Profile",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Sahil Patil\",\n  \"age\": 28,\n  \"gender\": \"MALE\",\n  \"religion\": \"Hindu\",\n  \"caste\": \"Maratha\",\n  \"subCaste\": \"96 Kuli\",\n  \"motherTongue\": \"Marathi\",\n  \"height\": 175.5,\n  \"weight\": 72.0,\n  \"education\": \"M.Tech Computer Science\",\n  \"occupation\": \"Tech Lead\",\n  \"annualIncome\": 1800000.0,\n  \"maritalStatus\": \"NEVER_MARRIED\",\n  \"city\": \"Pune\",\n  \"state\": \"Maharashtra\",\n  \"country\": \"India\",\n  \"aboutMe\": \"Updated details.\",\n  \"hobbies\": \"Coding, Hiking\",\n  \"lifestyle\": \"VEGETARIAN\",\n  \"familyDetails\": \"Nuclear family.\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/profiles",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "profiles"
              ]
            }
          }
        },
        {
          "name": "Get My Profile",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/profiles/me",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "profiles",
                "me"
              ]
            }
          }
        },
        {
          "name": "Get Profile By User ID",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/profiles/2",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "profiles",
                "2"
              ]
            }
          }
        },
        {
          "name": "Upload Photo",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "body": {
              "mode": "formdata",
              "formdata": [
                {
                  "key": "file",
                  "type": "file",
                  "src": []
                },
                {
                  "key": "main",
                  "value": "true",
                  "type": "text"
                }
              ]
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/profiles/photos",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "profiles",
                "photos"
              ]
            }
          }
        },
        {
          "name": "Delete Photo",
          "request": {
            "method": "DELETE",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/profiles/photos/1",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "profiles",
                "photos",
                "1"
              ]
            }
          }
        },
        {
          "name": "Get Profile Visitors",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/profiles/visitors",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "profiles",
                "visitors"
              ]
            }
          }
        }
      ]
    },
    {
      "name": "3. Partner Preferences",
      "item": [
        {
          "name": "Get My Preference Settings",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/preferences/my",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "preferences",
                "my"
              ]
            }
          }
        },
        {
          "name": "Update My Preferences",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              },
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"preferredAgeMin\": 22,\n  \"preferredAgeMax\": 28,\n  \"preferredHeightMin\": 150.0,\n  \"preferredHeightMax\": 170.0,\n  \"religion\": \"Hindu\",\n  \"caste\": \"Maratha\",\n  \"education\": \"Graduate\",\n  \"occupation\": \"Engineer\",\n  \"incomeMin\": 500000.0,\n  \"incomeMax\": 2000000.0,\n  \"city\": \"Mumbai\",\n  \"state\": \"Maharashtra\",\n  \"country\": \"India\",\n  \"lifestyle\": \"VEGETARIAN\"\n}"
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/preferences/my",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "preferences",
                "my"
              ]
            }
          }
        }
      ]
    },
    {
      "name": "4. Discover & Search",
      "item": [
        {
          "name": "Advanced Filters Search",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/search?gender=FEMALE&minAge=21&maxAge=28&religion=Hindu&caste=Maratha",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "search"
              ],
              "query": [
                {
                  "key": "gender",
                  "value": "FEMALE"
                },
                {
                  "key": "minAge",
                  "value": "21"
                },
                {
                  "key": "maxAge",
                  "value": "28"
                },
                {
                  "key": "religion",
                  "value": "Hindu"
                },
                {
                  "key": "caste",
                  "value": "Maratha"
                }
              ]
            }
          }
        },
        {
          "name": "Get Matched Recommendations",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/recommendations",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "recommendations"
              ]
            }
          }
        },
        {
          "name": "Get Profile Compatibility %",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/compatibility/3",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "compatibility",
                "3"
              ]
            }
          }
        }
      ]
    },
    {
      "name": "5. Match Interactions",
      "item": [
        {
          "name": "Express Interest",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/interests/send/3",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "interests",
                "send",
                "3"
              ]
            }
          }
        },
        {
          "name": "Accept Interest",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/interests/accept/1",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "interests",
                "accept",
                "1"
              ]
            }
          }
        },
        {
          "name": "Reject Interest",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/interests/reject/1",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "interests",
                "reject",
                "1"
              ]
            }
          }
        },
        {
          "name": "Get Received Interests",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/interests/received",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "interests",
                "received"
              ]
            }
          }
        },
        {
          "name": "Get Sent Interests",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/interests/sent",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "interests",
                "sent"
              ]
            }
          }
        },
        {
          "name": "Get Mutual Matches",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/mutual",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "mutual"
              ]
            }
          }
        },
        {
          "name": "Block User",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/block/4",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "block",
                "4"
              ]
            }
          }
        },
        {
          "name": "Report User Profile",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              },
              {
                "key": "Content-Type",
                "value": "application/x-www-form-urlencoded"
              }
            ],
            "body": {
              "mode": "urlencoded",
              "urlencoded": [
                {
                  "key": "reason",
                  "value": "Spam / Fake Profile",
                  "type": "text"
                },
                {
                  "key": "details",
                  "value": "Using internet pictures.",
                  "type": "text"
                }
              ]
            },
            "url": {
              "raw": "{{baseUrl}}/api/v1/matches/report/5",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "matches",
                "report",
                "5"
              ]
            }
          }
        }
      ]
    },
    {
      "name": "6. Messaging / Chat",
      "item": [
        {
          "name": "Get Chat History",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/chat/history/3",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "chat",
                "history",
                "3"
              ]
            }
          }
        },
        {
          "name": "Send Message HTTP",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/chat/send?receiverId=3&content=Hello! Nice connecting with you.",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "chat",
                "send"
              ],
              "query": [
                {
                  "key": "receiverId",
                  "value": "3"
                },
                {
                  "key": "content",
                  "value": "Hello! Nice connecting with you."
                }
              ]
            }
          }
        }
      ]
    },
    {
      "name": "7. Notifications",
      "item": [
        {
          "name": "Get My Notifications",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/notifications",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "notifications"
              ]
            }
          }
        },
        {
          "name": "Mark Alert Read",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/notifications/read/1",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "notifications",
                "read",
                "1"
              ]
            }
          }
        }
      ]
    },
    {
      "name": "8. Administration",
      "item": [
        {
          "name": "Get Dashboard Statistics",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/admin/stats",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "admin",
                "stats"
              ]
            }
          }
        },
        {
          "name": "Get Pending Verification Requests",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/admin/verifications/pending",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "admin",
                "verifications",
                "pending"
              ]
            }
          }
        },
        {
          "name": "Approve Profile Verification",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/admin/verifications/approve/2",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "admin",
                "verifications",
                "approve",
                "2"
              ]
            }
          }
        },
        {
          "name": "Get Reported Complaints",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/admin/reports",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "admin",
                "reports"
              ]
            }
          }
        },
        {
          "name": "Resolve / Suspend Report",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{jwtToken}}"
              }
            ],
            "url": {
              "raw": "{{baseUrl}}/api/v1/admin/reports/resolve/1?action=SUSPEND",
              "host": [
                "{{baseUrl}}"
              ],
              "path": [
                "api",
                "v1",
                "admin",
                "reports",
                "resolve",
                "1"
              ],
              "query": [
                {
                  "key": "action",
                  "value": "SUSPEND"
                }
              ]
            }
          }
        }
      ]
    }
  ]
}
```
