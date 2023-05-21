const express = require('express');
const bodyParser = require('body-parser');
const fs = require('fs');

const app = express();
app.use(bodyParser.json());

const dataFilePath = './component_state.json';

app.post('/component-state', (req, res) => {
    fs.writeFile(dataFilePath, JSON.stringify(req.body, null, 2), (err) => {
        if (err) {
            console.error(err);
            res.status(500).send({ message: 'An error occurred while saving the configuration.' });
        } else {
            res.status(200).send({ message: 'Configuration saved successfully.' });
        }
    });
});

app.get('/component-state', (req, res) => {
    fs.readFile(dataFilePath, 'utf-8', (err, data) => {
        if (err) {
            console.error(err);
            res.status(500).send({ message: 'An error occurred while loading the configuration.' });
        } else {
            res.status(200).send(JSON.parse(data));
        }
    });
});

const port = process.env.PORT || 3000;
app.listen(port, () => {
    console.log(`Server is listening on port ${port}`);
});

