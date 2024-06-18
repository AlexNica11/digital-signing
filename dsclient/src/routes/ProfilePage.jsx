import {NavLink} from "react-router-dom";
import {useEffect, useState} from "react";
import axios from "axios";
import secureLocalStorage from "react-secure-storage";

export default function ProfilePage(){
    const [keyStores, setKeyStores] = useState([]);

    useEffect(() => {
        axios({
            method: 'post',
            url: `/api/users/keyStores`,
            headers: {
                Authorization: "Bearer " + secureLocalStorage.getItem("securityToken"),
                Accept: 'application/json',
                'Content-Type': 'application/json'
            }
        }).then((res) => {
            console.log(res.data);
            setKeyStores(res.data);
        }).catch((error) => {
            console.error(error);
        });
    }, [])

    return (
        <>
            <h1>Profile</h1>
            <div>

            </div>
            <div>
                <NavLink to={"/uploadKeystore"}>Upload KeyStore</NavLink>
            </div>
            <div>
                <label>Key Stores</label>
                <ul>
                    {keyStores.map((ks) =>
                        <li key={ks}>
                            <NavLink to={"/keyStore/" + ks}>{ks}</NavLink>
                        </li>
                    )}
                </ul>
            </div>
        </>
    );
}