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
            <h3>Profile</h3>
            <div>
                <NavLink className="btn outlined icon-right light-blue-text accent-4" to={"/uploadKeystore"}>
                    Upload KeyStore
                    <i className="material-icons">add</i>
                </NavLink>
            </div>
            <div>
                <h4>Key Stores</h4>
                <ul className="collection">
                    {keyStores.map((ks) =>
                        <li key={ks}>
                            <NavLink className="collection-item active light-blue accent-4" to={"/keyStore/" + ks}>{ks}</NavLink>
                        </li>
                    )}
                </ul>
            </div>
        </>
    );
}
