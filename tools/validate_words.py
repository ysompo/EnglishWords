# tools/validate_words.py
# Usage: python tools/validate_words.py app/src/main/assets/words.json
import json
import sys

REQUIRED_FIELDS = {"id", "word", "translation_he", "part_of_speech", "example_sentence", "order_index"}
ALLOWED_POS = {"noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction", "determiner", "interjection", "number"}

def validate(path: str) -> list[str]:
    errors = []
    try:
        with open(path, encoding="utf-8") as f:
            words = json.load(f)
    except FileNotFoundError:
        return [f"File not found: {path}"]
    except json.JSONDecodeError as e:
        return [f"File is not valid JSON: {e}"]

    if not isinstance(words, list):
        return ["Top-level JSON must be an array"]

    if len(words) == 0:
        errors.append("words.json is empty (0 entries)")

    seen_ids = set()
    seen_order_indexes = set()
    seen_words = set()

    for i, entry in enumerate(words):
        if not isinstance(entry, dict):
            errors.append(f"Entry {i}: not a JSON object")
            continue

        missing = REQUIRED_FIELDS - entry.keys()
        if missing:
            errors.append(f"Entry {i}: missing fields {missing}")
            continue

        if entry["id"] in seen_ids:
            errors.append(f"Entry {i}: duplicate id {entry['id']}")
        seen_ids.add(entry["id"])

        if entry["order_index"] in seen_order_indexes:
            errors.append(f"Entry {i}: duplicate order_index {entry['order_index']}")
        seen_order_indexes.add(entry["order_index"])

        word_lower = entry["word"].strip().lower()
        if word_lower in seen_words:
            errors.append(f"Entry {i}: duplicate word '{entry['word']}'")
        seen_words.add(word_lower)

        if entry["part_of_speech"] not in ALLOWED_POS:
            errors.append(f"Entry {i} ('{entry['word']}'): invalid part_of_speech '{entry['part_of_speech']}'")

        if "___" not in entry["example_sentence"]:
            errors.append(f"Entry {i} ('{entry['word']}'): example_sentence missing '___' placeholder")

        if not entry["translation_he"].strip():
            errors.append(f"Entry {i} ('{entry['word']}'): empty translation_he")

    expected_ids = set(range(1, len(words) + 1))
    if seen_ids != expected_ids:
        errors.append(f"ids are not a contiguous 1..N range: got {len(seen_ids)} unique ids for {len(words)} entries")

    return errors

if __name__ == "__main__":
    path = sys.argv[1] if len(sys.argv) > 1 else "app/src/main/assets/words.json"
    problems = validate(path)
    if problems:
        print(f"FAILED: {len(problems)} problem(s) found in {path}")
        for p in problems[:50]:
            print(f" - {p}")
        sys.exit(1)
    print(f"OK: {path} is valid")
