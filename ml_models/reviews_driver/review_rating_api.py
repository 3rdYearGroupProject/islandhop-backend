from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch

app = Flask(__name__)

# Load rating model
rating_tokenizer = AutoTokenizer.from_pretrained("nlptown/bert-base-multilingual-uncased-sentiment")
rating_model = AutoModelForSequenceClassification.from_pretrained("nlptown/bert-base-multilingual-uncased-sentiment")

# Load improved toxicity model
toxicity_model_name = "Hate-speech-CNERG/dehatebert-mono-english"
toxicity_tokenizer = AutoTokenizer.from_pretrained(toxicity_model_name)
toxicity_model = AutoModelForSequenceClassification.from_pretrained(toxicity_model_name)

# Map label indices to readable categories
toxicity_labels = ['normal', 'offensive', 'hate']

@app.route('/analyze-review', methods=['POST'])
def analyze_review():
    data = request.json
    review = data.get('review', '')

    if not review.strip():
        return jsonify({'error': 'Empty review'}), 400

    # --- Sentiment Rating (1–5, then scaled to 0–10) ---
    rating_inputs = rating_tokenizer.encode_plus(review, return_tensors="pt", truncation=True)
    rating_outputs = rating_model(**rating_inputs)
    rating_probs = torch.nn.functional.softmax(rating_outputs.logits, dim=-1)
    rating_scores = torch.arange(1, 6)
    raw_rating = torch.sum(rating_probs * rating_scores).item()
    final_rating = round(raw_rating * 2, 1)  # Convert to 0–10 scale

    # --- Toxicity Detection ---
    tox_inputs = toxicity_tokenizer.encode_plus(review, return_tensors="pt", truncation=True)
    tox_outputs = toxicity_model(**tox_inputs)
    tox_probs = torch.nn.functional.softmax(tox_outputs.logits, dim=-1)
    tox_label = torch.argmax(tox_probs).item()
    tox_confidence = round(tox_probs[0][tox_label].item(), 4)
    tox_result = toxicity_labels[tox_label]

    # --- Add a note if it's borderline ---
    note = ""
    if tox_confidence < 0.6:
        note = "Toxicity classification is uncertain (low confidence)."

    return jsonify({
        'rating': final_rating,
        'toxicity': tox_result,
        'confidence': tox_confidence,
        'note': note
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
