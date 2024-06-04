import {useEffect, useState} from "react";
import axios from "axios";
import secureLocalStorage from "react-secure-storage";

export default function UploadKeyStorePage(){
    const [selectedFile, setSelectedFile] = useState(null);
    const [keyStorePassword, setKeyStorePassword] = useState("");
    const [privateKeyParams, setPrivateKeyParams] = useState([]);
    const [privateKeyParam, setPrivateKeyParam] = useState({
        alias: "",
        password: ""
    });

    // On file select (from the pop up)
    const onFileChange = (event) => {
        // Update the state
        setSelectedFile(event.target.files[0]);
    };

    // On file upload (click the upload button)
    const onFileUpload = () => {
        // Create an object of formData
        const formData = new FormData();

        let pkParams = [];
        pkParams.push({
            "alias": "good-user-crl-ocsp",
            "password": null
        });

        console.log(JSON.stringify(pkParams));

        // Update the formData object
        formData.append("file", selectedFile);
        formData.append("keyStoreName", selectedFile.name);
        formData.append("keyStorePassword", keyStorePassword);
        formData.append("privateKeyParams", JSON.stringify(privateKeyParams));

        // Details of the uploaded file
        console.log(selectedFile);

        // Request made to the backend api
        // Send formData object
        // axios.post("api/uploadKeyStoreForm", formData);
        axios({
            method: 'post',
            url: `/api/users/uploadKeyStoreForm`,
            data: formData,
            headers: {
                Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                'Content-Type': 'multipart/form-data'
            }
        });
    };

    // File content to be displayed after
    // file upload is complete
    const fileData =  () => {
        if (selectedFile) {
            return (
                <div>
                    <h2>File Details:</h2>
                    <p>
                        File Name:{" "}
                        {selectedFile.name}
                    </p>

                    <p>
                        File Type:{" "}
                        {selectedFile.type}
                    </p>

                    <p>
                        Last Modified:{" "}
                        {selectedFile.lastModified}
                    </p>
                </div>
            );
        } else {
            return (
                <div>
                    <br/>
                    <h4>
                        Choose before Pressing the Upload
                        button
                    </h4>
                </div>
            );
        }
    };

    useEffect(() => {
        setSelectedFile(null);
    }, [])

    

    const handleKeyStorePassword = (e) => {
        const { name, value } = e.target;
        setKeyStorePassword(value);
    };

    const handleInput = (e) => {
        const { name, value } = e.target;
        setPrivateKeyParam((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmitKeyStoreEntry = (e) => {
        e.preventDefault();
        console.log("alias: ", privateKeyParam.alias);
        console.log("password: ", privateKeyParam.password);
        if (privateKeyParam.username !== "") {
            if(privateKeyParam.password === ""){
                privateKeyParam.password = null;
            }
            setPrivateKeyParams([
                ...privateKeyParams,
                privateKeyParam
            ])
            console.log(privateKeyParams)
            return;
        }
        alert("please provide a valid input");
    };

    return (
        <>
            <div>
                <h3>KeyStore Upload</h3>
                <div>
                    <input
                        type="file"
                        onChange={onFileChange}
                    />
                    <button onClick={onFileUpload}>
                        Upload!
                    </button>
                    <div className="form_control">
                        <label htmlFor="keyStorePassword">KeyStorePassword</label>
                        <input id="keyStorePassword" name="keyStorePassword" onChange={handleKeyStorePassword}/>
                    </div>
                </div>
                {fileData()}
            </div>
            <form onSubmit={handleSubmitKeyStoreEntry}>
                <div className="form_control">
                <label htmlFor="alias">alias</label>
                    <input id="alias" name="alias" onChange={handleInput}/>
                </div>
                <div className="form_control">
                    <label htmlFor="password">password</label>
                    <input id="password" name="password" onChange={handleInput}/>
                </div>
                <button className="btn-submit">Submit KeyStore Entry</button>
            </form>
            <div>
                <label>Key Store Entries</label>
                <ul>
                    {privateKeyParams.map((pk) =>
                        <li key={pk.alias}>
                            <label>alias: {pk.alias}</label>
                            <br/>
                            <label>password: {pk.password}</label>
                        </li>
                    )}
                </ul>
            </div>
        </>
    );
}
