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

    // On file upload (click the upload button)
    const onFileUpload = () => {
        if(selectedFile.type === 'application/x-pkcs12') {
            // Create an object of formData
            const formData = new FormData();

            // Update the formData object
            formData.append("file", selectedFile);
            formData.append("keyStoreName", selectedFile.name);
            formData.append("keyStorePassword", keyStorePassword);
            formData.append("privateKeyParams", JSON.stringify(privateKeyParams));

            // Request made to the backend api
            // Send formData object
            axios({
                method: 'post',
                url: `/api/users/uploadKeyStoreForm`,
                data: formData,
                headers: {
                    Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                    'Content-Type': 'multipart/form-data'
                }
            });
            setSelectedFile(null);
            setKeyStorePassword("");
            setPrivateKeyParams([]);
            setPrivateKeyParam({
                alias: "",
                password: ""
            });
        } else {
            console.error('Only PKCS#12 files allowed')
        }
    };

    // On file select (from the pop up)
    const onFileChange = (event) => {
        // Update the state
        setSelectedFile(event.target.files[0]);
    };

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
        if (privateKeyParam.username !== "") {
            if(privateKeyParam.password === ""){
                privateKeyParam.password = null;
            }
            setPrivateKeyParams([
                ...privateKeyParams,
                privateKeyParam
            ])
            return;
        }
        alert("please provide a valid input");
    };

    // File content to be displayed after
    // file upload is complete
    const fileData =  () => {
        if (selectedFile) {
            return (
                <div>
                    <h4>File Details:</h4>
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
                        Choose key store file and set entries before uploading
                    </h4>
                </div>
            );
        }
    };



    // const handleInput = (e) => {
    //     const { name, value } = e.target;
    //     setInput((prev) => ({
    //         ...prev,
    //         [name]: value,
    //     }));
    // };

    /*
            <form onSubmit={handleSubmitEvent}>
            <div className="form_control">
                <label htmlFor="password">Password:</label>
                <input
                    type="password"
                    id="password"
                    name="password"
                    aria-describedby="user-password"
                    aria-invalid="false"
                    onChange={handleInput}
                />
                <div id="user-password" className="sr-only">
                    your password should be more than 6 character
                </div>
            </div>
            <button className="btn-submit">Submit</button>
        </form>
     */

    /*
    const handleSubmitEvent = (e) => {
        e.preventDefault();
        console.log("email: ", input.email);
        console.log("username: ", input.username);
        console.log("password: ", input.password);
        if (input.username !== "" && input.password !== "") {
            auth.loginAction(input).then(r => console.log(r));
            return;
        }
        alert("please provide a valid input");
    };
     */



    return (
        <>
            <div>
                <h3>Key Store Upload</h3>
                <div>
                    <input
                        type="file"
                        onChange={onFileChange}
                        className="btn-large"
                    />
                    <button className="btn elevated" onClick={onFileUpload}>
                        Upload!
                    </button>
                    <div className="s12 m6 input-field outlined">
                        <input id="keyStorePassword" type="text" name="keyStorePassword" placeholder=" "
                               onChange={handleKeyStorePassword}/>
                        <label htmlFor="keyStorePassword">Key Store Password</label>
                    </div>
                </div>
                {fileData()}
            </div>
            <form onSubmit={handleSubmitKeyStoreEntry}>
                <h5>Set key store entry:</h5>
                <div className="s12 m6 input-field outlined">
                    <input id="alias" name="alias" type="text" placeholder=" " onChange={handleInput}/>
                    <label htmlFor="alias">Alias</label>
                </div>
                <br/>
                <div className="s12 m6 input-field outlined">
                    <input id="password" name="password" type="text" placeholder=" " onChange={handleInput}/>
                    <label htmlFor="password">Password</label>
                </div>
                <br/>
                <button className="btn-submit btn outlined icon-right">
                    Submit Key Store Entry
                    <i className="material-icons">add</i>
                </button>
            </form>
            <blockquote>Key store entry password can be left empty if there is no password.</blockquote>
            <div>
                <h5>Key Store Entries</h5>
                <ul className="collection">
                    {privateKeyParams.map((pk) =>
                        <li className="collection-item" key={pk.alias}>
                            <table className="responsive-table">
                                <thead>
                                    <tr>
                                        <th>Alias</th>
                                        <th>Password</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr>
                                        <td>{pk.alias}</td>
                                        <td>{pk.password}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </li>
                    )}
                </ul>
            </div>
        </>
    );
}
