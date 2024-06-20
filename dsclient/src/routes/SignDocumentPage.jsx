import {useEffect, useState} from "react";
import secureLocalStorage from "react-secure-storage";
import axios from "axios";

export default function SignDocumentPage(){
    const [selectedFile, setSelectedFile] = useState(null);
    const [signatures, setSignatures] = useState(["CAdES_BASELINE_"]);
    const [signatureLevel, setSignatureLevel] = useState("B");

    const [params, setParams] = useState({
        signature: "CAdES_BASELINE_",
        extendSignature: false,
        keyStoreName: "",
        privateKeyAlias: ""
    });

    const handleSignature = (signature) => {
        setParams((prev) => ({
            ...prev,
            ["signature"]: signature.target.value,
        }));
    }

    // On file select (from the pop up)
    const onFileChange = (event) => {
        // Update the state
        let file = event.target.files[0];
        setSelectedFile(file);
        switch (file.type){
            case "application/pdf":
                setSignatures(["PAdES_BASELINE_"]);
                setParams((prev) => ({
                    ...prev,
                    ["signature"]: "PAdES_BASELINE_",
                }));
                break;
            case "application/xml":
            case "text/xml":
            case "application/atom+xml":
                setSignatures(["XAdES_BASELINE_"]);
                setParams((prev) => ({
                    ...prev,
                    ["signature"]: "XAdES_BASELINE_",
                }));
                break;
            case "application/json":
                setSignatures(["JAdES_BASELINE_", "CAdES_BASELINE_"]);
                setParams((prev) => ({
                    ...prev,
                    ["signature"]: "JAdES_BASELINE_",
                }));
                break;
            default:
                setSignatures(["CAdES_BASELINE_", "JAdES_BASELINE_"]);
                setParams((prev) => ({
                    ...prev,
                    ["signature"]: "CAdES_BASELINE_",
                }));
        }
    };

    // On file upload (click the upload button)
    const onFileUpload = () => {
        // Create an object of formData
        const formData = new FormData();

        // Update the formData object
        formData.append("document", selectedFile);
        formData.append("documentName", selectedFile.name);
        formData.append("signature", params.signature + signatureLevel);
        formData.append("extendSignature", params.extendSignature === "true");
        formData.append("keyStoreParams", JSON.stringify({
            keyStoreBytes: "",
            keyStoreName: params.keyStoreName,
            keyStorePassword: "",
            privateKeyParams: [
                {
                    alias: params.privateKeyAlias,
                    password: null
                }
            ]
        }));

        // Details of the uploaded file
        console.log(selectedFile.name);
        console.log(params.signature + signatureLevel);

        // Request made to the backend api
        // Send formData object
        axios({
            method: 'post',
            url: `/api/service/signWithFormData`,
            data: formData,
            headers: {
                Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                'Content-Type': 'multipart/form-data'
            }
        });
    };

    useEffect(() => {
        setSelectedFile(null);
    }, [])

    const handleInput = (e) => {
        const { name, value } = e.target;
        setParams((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSignatureLevel = (e) => {
        const { name, value } = e.target;
        setSignatureLevel(value);
    }

    return (
        <>
            <div className="">
                <h3>Sign a document of choice</h3>
                <div>
                    <br/>
                    <br/>
                    <input
                        type="file"
                        onChange={onFileChange}
                        className="btn-large"
                    />
                    <div className="">
                        <label htmlFor="signature"><h5>Signature</h5></label>
                        <select className="browser-default" name="signature" id="signature" onChange={handleSignature}>
                            {signatures.map((sig) =>
                                <option key={sig} value={sig}>{sig.slice(0, -1)}</option>
                            )}
                        </select>
                        <label htmlFor="signatureLevel"><h5>Signature Level</h5></label>
                        <select className="browser-default" name="signatureLevel" id="signatureLevel"
                                onChange={handleSignatureLevel}>
                            <option value="B">B</option>
                            <option value="T">T</option>
                            <option value="LT">LT</option>
                            <option value="LTA">LTA</option>
                        </select>
                        <h5>{params.signature + signatureLevel}</h5>
                        <label htmlFor="extendSignature"><h5>Extend Signature</h5></label>
                        <select className="browser-default" name="extendSignature" id="extendSignature"
                                onChange={handleInput}>
                            <option value="false">false</option>
                            <option value="true">true</option>
                        </select>
                        {/*<label>{params.extendSignature}</label>*/}
                    </div>
                    <div className="form_control">
                        <label htmlFor="keyStoreName"><h5>Key Store Name</h5></label>
                        <input id="keyStoreName" name="keyStoreName" onChange={handleInput}/>
                        <label>{params.keyStoreName}</label>
                    </div>
                    <div className="form_control">
                        <label htmlFor="privateKeyAlias"><h5>Private Key Alias</h5></label>
                        <input id="privateKeyAlias" name="privateKeyAlias" onChange={handleInput}/>
                        <label>{params.privateKeyAlias}</label>
                    </div>
                    <br/>
                    <button className="btn elevated" onClick={onFileUpload}>
                        Sign!
                    </button>
                </div>
            </div>
        </>
    );
}
