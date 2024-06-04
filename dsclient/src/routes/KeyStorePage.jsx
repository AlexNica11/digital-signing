import {NavLink, useParams} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import secureLocalStorage from "react-secure-storage";

export default function KeyStorePage(){
    let {keyStoreName} = useParams();
    const [pkParams, setPkParams] = useState([]);

    useEffect(() => {
        axios({
            method: 'post',
            url: `/api/users/privateKeyParams`,
            data: keyStoreName,
            headers: {
                Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                Accept: 'application/json',
                'Content-Type': 'text/plain'
            }
        }).then((res) => {
            console.log(res.data);
            setPkParams(res.data);
        }).catch((error) => {
            console.error(error);
        });
    }, [])

    console.log(keyStoreName);

    return (
        <>
            <h1>KeyStore {keyStoreName}</h1>
            <div>
                <label>Key Store Entries</label>
                <ul>
                    {pkParams.map((pk) =>
                        <li key={pk}>
                            <label>{pk}</label>
                        </li>
                    )}
                </ul>
            </div>
        </>
    );
}
