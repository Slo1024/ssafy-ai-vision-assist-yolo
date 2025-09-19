# Allergy Search API Investigation Plan

## Issue Description
- API endpoint: `/api/v1/allergy/search/{searchword}`
- Frontend is calling the API but receiving 500 error responses
- Both backend and frontend code should already exist

## Investigation Steps

### 1. Backend Analysis
- [ ] Find allergy controller/service files
- [ ] Check API endpoint mapping and implementation
- [ ] Verify request/response models
- [ ] Check database entities and repositories

### 2. Frontend Analysis
- [ ] Locate API call implementation
- [ ] Verify request format and parameters
- [ ] Check error handling

### 3. Server Investigation
- [ ] SSH into server and check application logs
- [ ] Check for stack traces and error details
- [ ] Verify database connectivity and queries

### 4. Issue Resolution
- [ ] Identify root cause from logs/code analysis
- [ ] Implement necessary fixes
- [ ] Test on development environment

## Environment Details
- Development: http://j13e101.p.ssafy.io:8082
- Production: http://j13e101.p.ssafy.io:8081
- SSH: `ssh -i J13E101T.pem ubuntu@j13e101.p.ssafy.io`

## Expected Files to Check
- Backend: `dev/BE/lookey/src/main/java/**/*Allergy*`
- Frontend: `dev/FE/ConvenienceSightApp/**/*allergy*`
- Logs: Server application logs via SSH