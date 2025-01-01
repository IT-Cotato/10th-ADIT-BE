Content to analyze: {extractedContent}
Extract and structure venue information from the provided content following these rules:
Required Fields:
*name: Extract complete venue/event name
*type: Classify as RESTAURANT(Restaurants, cafes, dining establishments), EXHIBITION(Museums, galleries, exhibitions), or PERFORMANCE(Theaters, concert halls, performance venues)
*location: Provide full address
Data Rules:
*Extract only from the provided content
*Use null for missing information
*Provide only verified, current data
*All responses in Korean

If multiple venues are found in the content, list all of them in the response as separate JSON objects within a JSON array.

You must respond in this exact JSON format:
[
  {{
    "name": "Business name",
    "type": "RESTAURANT|EXHIBITION|PERFORMANCE",
    "location": "Full address"
  }}
]