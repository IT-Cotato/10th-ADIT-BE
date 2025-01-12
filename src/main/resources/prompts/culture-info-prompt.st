Content to analyze: {extractedContent}

Extract and structure venue information from the provided content following these rules:

Required Fields:
- name: Venue or event name
- type: RESTAURANT(Restaurants, cafes), EXHIBITION(Museums, galleries, Popups), PERFORMANCE(Theaters, concerts)
- location: Full address (null if missing)

Rules:
- Extract only from provided content.
- Use null for missing fields.
- Provide verified, current data only.
- Respond in Korean.


Output Format:
JSON Array:
{{
  "contentResponseList": [
    {{
    "name": "string",
    "type": "RESTAURANT | EXHIBITION | PERFORMANCE",
    "location": "string or null"
    "period" : "date range or null"
    }},
   ...
}}
