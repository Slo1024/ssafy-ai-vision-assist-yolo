# Allergy Search API Complete Analysis & Fix Plan

## Problem Statement
- API endpoint: `/api/v1/allergy/search/{searchword}`
- Frontend receives 500 Internal Server Error
- Need complete function analysis and perfect fix

## Deep Analysis of Potential Issues

### 1. JPQL Constructor Syntax Issue (HIGH PRIORITY)
**Location**: `AllergyListRepository.java:16`
```java
// CURRENT (INCORRECT):
SELECT new com.project.lookey.allergy.dto.AllergySearchResponse$Item(...)

// SHOULD BE:
SELECT new com.project.lookey.allergy.dto.AllergySearchResponse.Item(...)
```
**Impact**: This will cause ClassNotFoundException or JPQL parsing errors

### 2. Database Schema & Data Issues (HIGH PRIORITY)
**Potential Problems**:
- `AllergyList` table might not exist
- Table might be empty (no allergy data)
- Column names might not match entity fields
- Database collation issues with Korean search terms

**Need to Check**:
- Entity mapping in `AllergyList.java`
- Table structure in database
- Sample data existence
- Character encoding setup

### 3. Authentication Inconsistency (MEDIUM PRIORITY)
**Observation**:
- Other endpoints use `@AuthenticationPrincipal(expression = "userId") Integer userId`
- Search endpoint doesn't require authentication
- This might be intentional (public search) or a security issue

### 4. Parameter Encoding Issues (MEDIUM PRIORITY)
**Potential Problems**:
- Korean characters in search terms
- URL encoding/decoding issues
- Special characters causing SQL injection attempts

### 5. Spring Boot Configuration (LOW PRIORITY)
**Potential Problems**:
- JPA configuration issues
- Database connection problems
- Transaction management issues

## Investigation Plan

### Phase 1: Database Investigation
1. **Check Entity Definition**: Examine `AllergyList.java` entity
2. **Verify Database Schema**: Check if table exists and structure
3. **Confirm Sample Data**: Verify there's data to search
4. **Test Database Connection**: Ensure connectivity works

### Phase 2: Code Analysis
1. **Fix JPQL Constructor**: Correct the syntax issue
2. **Review Entity Mapping**: Ensure @Entity, @Table, @Column annotations are correct
3. **Check Authentication**: Determine if search should be public or authenticated
4. **Validate Parameter Handling**: Ensure proper encoding/decoding

### Phase 3: Comprehensive Testing
1. **Unit Testing**: Test repository query directly
2. **Integration Testing**: Test full API endpoint
3. **Edge Case Testing**: Empty strings, special characters, Korean text
4. **Error Handling**: Verify proper error responses

## Files to Examine and Potentially Fix

### Backend Files
- `AllergyListRepository.java` - Fix JPQL constructor syntax
- `AllergyList.java` - Verify entity mapping
- `AllergyService.java` - Check parameter validation
- `AllergyController.java` - Review authentication and error handling

### Configuration Files
- `application.yml/properties` - Database and JPA configuration
- Database migration scripts - Schema definition

### Frontend Files (if needed)
- `AllergyRepositoryImpl.kt` - Error handling
- `ApiService.kt` - Parameter encoding

## Specific Fix Implementation Plan

### Step 1: Entity & Database Verification
```java
// Check AllergyList entity for proper annotations
@Entity
@Table(name = "allergy_list")
public class AllergyList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;
    // ...
}
```

### Step 2: Repository Query Fix
```java
// Fix the JPQL query constructor syntax
@Query("""
    SELECT new com.project.lookey.allergy.dto.AllergySearchResponse.Item(
        al.id,
        al.name
    )
    FROM AllergyList al
    WHERE al.name LIKE CONCAT('%', :keyword, '%')
    ORDER BY al.name
""")
```

### Step 3: Service Enhancement
```java
// Add proper validation and error handling
public AllergySearchResponse searchAllergies(String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
        return new AllergySearchResponse(Collections.emptyList());
    }

    String sanitizedKeyword = keyword.trim();

    try {
        var items = allergyListRepository.findNamesByKeyword(sanitizedKeyword);
        return new AllergySearchResponse(items);
    } catch (Exception e) {
        log.error("Error searching allergies with keyword: {}", sanitizedKeyword, e);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
            "알레르기 검색 중 오류가 발생했습니다.");
    }
}
```

### Step 4: Controller Enhancement
```java
// Consider adding authentication if needed and better error handling
@GetMapping("/search/{searchword}")
public ResponseEntity<?> search(
        @PathVariable("searchword") String searchword
) {
    try {
        AllergySearchResponse data = allergyService.searchAllergies(searchword);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "알레르기 검색 성공",
                "result", data
        ));
    } catch (ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "status", e.getStatusCode().value(),
                "message", e.getReason(),
                "result", Collections.emptyMap()
        ));
    }
}
```

## Testing Strategy

### 1. Database Testing
- Verify `allergy_list` table exists
- Check sample data: `SELECT * FROM allergy_list LIMIT 10;`
- Test manual query: `SELECT id, name FROM allergy_list WHERE name LIKE '%검색어%';`

### 2. API Testing
- Test with simple English terms: `/api/v1/allergy/search/milk`
- Test with Korean terms: `/api/v1/allergy/search/우유`
- Test with special characters: `/api/v1/allergy/search/test%20space`
- Test with empty string: `/api/v1/allergy/search/`

### 3. Error Scenarios
- Test with very long search terms
- Test with SQL injection attempts
- Test with Unicode characters

## Success Criteria
1. ✅ API returns 200 status for valid searches
2. ✅ Returns appropriate empty results for no matches
3. ✅ Handles Korean text correctly
4. ✅ Proper error messages for invalid inputs
5. ✅ No SQL injection vulnerabilities
6. ✅ Frontend successfully displays search results

## Next Steps
1. Execute Phase 1 investigations
2. Implement fixes based on findings
3. Test thoroughly with various scenarios
4. Deploy to development environment
5. Validate with frontend integration