from flask import Flask, jsonify, render_template

app = Flask(__name__)

attacks = []

#Redirect to main dashboard 
@app.route("/")
def dashboard():
    return render_template("index.html")

#redirect to attack details
@app.route("/api/attacks")
def get_attacks():
    return jsonify(attacks)


@app.route("/api/test")
def test_attack():

    attack = {
        "ip": "8.8.8.8",
        "country": "United States",
        "city": "Mountain View",
        "latitude": 37.4056,
        "longitude": -122.0775,
        "command": "whoami"
    }

    attacks.append(attack)

    return jsonify(attack)


if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=5000
    )